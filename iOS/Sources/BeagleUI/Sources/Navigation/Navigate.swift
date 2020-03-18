//
//  Copyright © 08/11/19 Zup IT. All rights reserved.
//

/// Action to represent a screen transition
public enum Navigate: Action {
    var newPath: NewPath? {
        switch self {
        case .addView(let newPath), .swapView(let newPath), .presentView(let newPath):
            return newPath

        case .swapScreen, .addScreen, .presentScreen, .finishView, .popView, .popToView, .openDeepLink:
            return nil
        }
    }
    
    case openDeepLink(DeepLinkNavigation)
    
    case swapScreen(Screen)
    case swapView(NewPath)
    
    case addScreen(Screen)
    case addView(NewPath)
    
    case presentScreen(Screen)
    case presentView(NewPath)

    case finishView
    case popView
    case popToView(Path)

    public typealias Path = String
    public typealias Data = [String: String]

    public struct NewPath {
        public let path: Path
        public let shouldPrefetch: Bool
        public let fallback: Screen?
        
        public init(path: Path, shouldPrefetch: Bool = false, fallback: Screen? = nil) {
            self.path = path
            self.shouldPrefetch = shouldPrefetch
            self.fallback = fallback
        }
    }
    
    public struct DeepLinkNavigation {
        public let path: Path
        public let data: Data?
        public let component: ServerDrivenComponent?

        public init(path: Path, data: Data? = nil, component: ServerDrivenComponent? = nil) {
            self.path = path
            self.data = data
            self.component = component
        }
    }
}

extension Navigate: Decodable {
    enum CodingKeys: String, CodingKey {
        case type
        case path
        case shouldPrefetch
        case screen
        case data
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let type = try container.decode(NavigateEntity.NavigationType.self, forKey: .type)
        let path = try container.decodeIfPresent(String.self, forKey: .path)
        let shouldPrefetch = try container.decodeIfPresent(Bool.self, forKey: .shouldPrefetch)
        let screen = try container.decodeIfPresent(ScreenComponent.self, forKey: .screen)
        let data = try container.decodeIfPresent([String: String].self, forKey: .data)
        self = try NavigateEntity(type: type, path: path, shouldPrefetch: shouldPrefetch, screen: screen, data: data).mapToUIModel()
    }
}

struct NavigateEntity {
    let type: NavigationType
    let path: String?
    let shouldPrefetch: Bool?
    let screen: ScreenComponent?
    let data: [String: String]?
    
    enum NavigationType: String, Decodable, CaseIterable {
        case openDeepLink = "OPEN_DEEP_LINK"
        case swapView = "SWAP_VIEW"
        case addView = "ADD_VIEW"
        case finishView = "FINISH_VIEW"
        case popView = "POP_VIEW"
        case popToView = "POP_TO_VIEW"
        case presentView = "PRESENT_VIEW"
    }
    
    enum Destination {
        case declarative(Screen)
        case remote(Navigate.NewPath)
    }

    struct Error: Swift.Error {
        let reason: String
    }

    func mapToUIModel() throws -> Navigate {
        switch type {
        case .popToView:
            let path = try usePath()
            return .popToView(path)
            
        case .openDeepLink:
            let path = try usePath()
            return .openDeepLink(.init(path: path, data: data))
            
        case .swapView:
            switch try destination() {
            case .declarative(let screen):
                return .swapScreen(screen)
            case .remote(let newPath):
                return .swapView(newPath)
            }
            
        case .addView:
            switch try destination() {
            case .declarative(let screen):
                return .addScreen(screen)
            case .remote(let newPath):
                return .addView(newPath)
            }
            
        case .presentView:
            switch try destination() {
            case .declarative(let screen):
                return .presentScreen(screen)
            case .remote(let newPath):
                return .presentView(newPath)
            }

        case .finishView:
            return .finishView
            
        case .popView:
            return .popView
        }
    }

    func usePath() throws -> String {
        guard let path = self.path else {
            throw Error(reason: "Error: Navigate of `type` \(type), should have property `path`")
        }
        return path
    }
    
    func destination() throws -> Destination {
        let screen = self.screen?.toScreen()
        if let screen = screen, path == nil {
            return .declarative(screen)
        }
        if let path = self.path {
            return .remote(.init(
                path: path,
                shouldPrefetch: shouldPrefetch ?? false,
                fallback: screen))
        }
        throw Error(reason: "Error: Navigate of `type` \(type), should have property `path` or `screen`")
    }
}

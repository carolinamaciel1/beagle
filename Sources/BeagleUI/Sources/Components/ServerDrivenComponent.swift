//
//  Copyright © 2019 Daniel Tes. All rights reserved.
//

import UIKit

public protocol ServerDrivenComponent: Renderable {}

public protocol ComposeComponent: ServerDrivenComponent {
    func build() -> ServerDrivenComponent
}

public protocol Renderable {
    typealias Dependencies =
        DependencyFlexViewConfigurator
        & DependencyTheme
        & DependencyValidatorProvider
        & DependencyPreFetching
        & DependencyAppBundle
        & DependencyNetwork
    
    func toView(context: BeagleContext, dependencies: Renderable.Dependencies) -> UIView
}

extension ServerDrivenComponent {
    public func toScreen() -> Screen {
        if let screenComponent = self as? ScreenComponent {
            return Screen(
                safeArea: screenComponent.safeArea,
                navigationBar: screenComponent.navigationBar,
                header: screenComponent.header,
                content: screenComponent.content,
                footer: screenComponent.footer
            )
        }
        return Screen(
            safeArea: SafeArea(top: true, leading: true, bottom: true, trailing: true),
            navigationBar: nil,
            header: nil,
            content: self,
            footer: nil
        )
    }
}

// Defines a representation of an unknwon Component
public struct AnyComponent: ServerDrivenComponent {
    public let value: Any
    
    public init(value: Any) {
        self.value = value
    }
}

extension AnyComponent: Renderable {
    public func toView(context: BeagleContext, dependencies: Renderable.Dependencies) -> UIView {
        let label = UILabel(frame: .zero)
        label.numberOfLines = 2
        label.text = "Unknown Component of type:\n \(String(describing: value))"
        label.textColor = .red
        label.backgroundColor = .yellow
        return label
    }
}

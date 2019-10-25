//
//  ServerDrivenScreenLoaderTests.swift
//  BeagleFrameworkTests
//
//  Created by Eduardo Sanches Bocato on 25/10/19.
//  Copyright © 2019 Daniel Tes. All rights reserved.
//

import XCTest
@testable import BeagleUI

final class ServerDrivenScreenLoaderTests: XCTestCase {

    func test_public_init_shouldSetupDependenciesProperly() {
        // Given / When
        let sut = ServerDrivenScreenLoading()
        let mirror = Mirror(reflecting: sut)
        let widgetFetcher = mirror.firstChild(of: ServerDrivenWidgetFetching.self)
        let viewBuilder = mirror.firstChild(of: BeagleViewBuilding.self)
        
        // Then
        XCTAssertNotNil(widgetFetcher)
        XCTAssertNotNil(viewBuilder)
    }
    
    func test_whenFetcherFails_itShouldReturnFetchError() {
        // Given
        let fetcherStub = ServerDrivenWidgetFetcherStub(
            resultToReturn: .failure(.emptyData)
        )
        let sut = ServerDrivenScreenLoading(
            widgetFetcher: fetcherStub,
            viewBuilder: BeagleViewBuilderDummy()
        )
        guard let url = URL(string: "www.something.com") else {
            XCTFail("Could not create URL.")
            return
        }
        
        // When
        let loadScreenExpectation = expectation(description: "loadScreenExpectation")
        var errorThrown: ServerDrivenScreenLoaderError?
        sut.loadScreen(from: url) { result in
            if case let .failure(error) = result {
                errorThrown = error
            }
            loadScreenExpectation.fulfill()
        }
        wait(for: [loadScreenExpectation], timeout: 1.0)
        
        // Then
        XCTAssertNotNil(errorThrown, "Expected an error, but got nil.")
        guard case .fetchError = errorThrown else {
            XCTFail("Expected a `.fetchError` error, but got \(String(describing: errorThrown)).")
            return
        }
    }
    
    func test_whenFetcherSucceeds_thenViewBuilderShouldBeCalledForRootWidget() {
        // Given
        let fetcherStub = ServerDrivenWidgetFetcherStub(
            resultToReturn: .success(Text("Something."))
        )
        let viewBuilderSpy = BeagleViewBuilderSpy()
        let sut = ServerDrivenScreenLoading(
            widgetFetcher: fetcherStub,
            viewBuilder: viewBuilderSpy
        )
        guard let url = URL(string: "www.something.com") else {
            XCTFail("Could not create URL.")
            return
        }
        
        // When
        let loadScreenExpectation = expectation(description: "loadScreenExpectation")
        var viewReturned: UIView?
        sut.loadScreen(from: url) { result in
            if case let .success(view) = result {
                viewReturned = view
            }
            loadScreenExpectation.fulfill()
        }
        wait(for: [loadScreenExpectation], timeout: 1.0)
        
        // Then
        XCTAssertNotNil(viewReturned, "Expected an `UIView`, but got nil.")
        XCTAssertTrue(viewBuilderSpy.buildFromRootWidgetCalled, "`buildFromRootWidget` should have been called.")
        XCTAssertTrue(viewBuilderSpy.widgetPassed is Text, "Expected to pass a `Text`, but got something else.")
    }

}

// MARK: - Testing Helpers

final class ServerDrivenWidgetFetcherStub: ServerDrivenWidgetFetcher {
    
    private let resultToReturn: Result<Widget, ServerDrivenWidgetFetcherError>
    init(resultToReturn: Result<Widget, ServerDrivenWidgetFetcherError>) {
        self.resultToReturn = resultToReturn
    }
    
    func fetchWidget(from url: URL, completion: @escaping (Result<Widget, ServerDrivenWidgetFetcherError>) -> Void) {
        completion(resultToReturn)
    }
    
}

final class BeagleViewBuilderDummy: BeagleViewBuilder {
    func buildFromRootWidget(_ widget: Widget) -> UIView {
        return UIView()
    }
}

final class BeagleViewBuilderSpy: BeagleViewBuilder {
    
    private(set) var buildFromRootWidgetCalled = false
    private(set) var widgetPassed: Widget?
    var viewToReturn: UIView?
    func buildFromRootWidget(_ widget: Widget) -> UIView {
        buildFromRootWidgetCalled = true
        widgetPassed = widget
        return viewToReturn ?? UIView()
    }
    
}

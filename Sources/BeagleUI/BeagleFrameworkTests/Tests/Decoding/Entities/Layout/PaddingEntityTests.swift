//
//  PaddingEntityTests.swift
//  BeagleFrameworkTests
//
//  Created by Eduardo Sanches Bocato on 19/09/19.
//  Copyright © 2019 Daniel Tes. All rights reserved.
//

import XCTest
@testable import BeagleUI

final class PaddingEntityTests: XCTestCase {
    
    func test_whenMapToWidgetIsCalled_thenItShouldReturnAPaddingWidget() {
        // Given
        let content = TextEntity(text: "text")
        let value = PaddingValueEntity(top: nil, left: nil, right: nil, bottom: nil)
        let child = AnyDecodableContainer(content: content)
        let sut = PaddingEntity(value: value, child: child)

        // When
        let padding = try? sut.mapToWidget()

        // Then
        XCTAssertNotNil(padding, "The Padding widget should not be nil.")
        XCTAssertTrue(padding is Padding)
    }
}

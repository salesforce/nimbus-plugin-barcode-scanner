//
//  BarcodeScannerTests.swift
//  BarcodeScannerTests
//
//  Created by Paul Tiarks on 7/30/20.
//  Copyright © 2020 Salesforce. All rights reserved.
//

import XCTest
@testable import BarcodeScanner
import SnapshotTesting

class BarcodeScannerTests: XCTestCase {

    func testBlankState() {
        let vc = BarcodeScannerViewController()
        assertSnapshot(matching: vc, as: .image)
    }

    func testNormalText() {
        let vc = BarcodeScannerViewController(instructionText: "Position barcode in the scanner view")
        assertSnapshot(matching: vc, as: .image)
    }

    func testVeryLongText() {
        let vc = BarcodeScannerViewController(instructionText: "This is a very long line of text that is testing what the scanner looks like when the text doesn't fit")
        assertSnapshot(matching: vc, as: .image)
    }

}

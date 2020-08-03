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

}

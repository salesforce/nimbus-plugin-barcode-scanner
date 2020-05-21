//
// Copyright (c) 2020, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import Foundation
import AVFoundation

public struct Barcode: Codable {
    let type: BarcodeType
    let value: String
}

extension Barcode {
    public init?(machineReadableCode: AVMetadataMachineReadableCodeObject) {
        guard let stringValue = machineReadableCode.stringValue else {
            return nil
        }
        value = stringValue
        switch machineReadableCode.type {
        case .code128:
            type = .code128
        case .code39:
            type = .code39
        case .code93:
            type = .code93
        case .dataMatrix:
            type = .dataMatrix
        case .ean13:
            type = .ean13
        case .ean8:
            type = .ean8
        case .interleaved2of5:
            type = .itf
        case .upce:
            type = .upce
        case .pdf417:
            type = .pdf417
        case .qr:
            type = .qr
        default:
            return nil
        }
    }
}

public enum BarcodeType: String, Codable, CaseIterable {
    case code128
    case code39
    case code93
    case dataMatrix
    case ean13
    case ean8
    case itf
    case upce
    case pdf417
    case qr
}

extension BarcodeType {

    public var metadataObjectType: AVMetadataObject.ObjectType {
        get {
            switch self {
            case .code128:
                return .code128
            case .code39:
                return .code39
            case .code93:
                return .code93
            case .dataMatrix:
                return .dataMatrix
            case .ean13:
                return .ean13
            case .ean8:
                return .ean8
            case .itf:
                return .interleaved2of5
            case .upce:
                return .upce
            case .pdf417:
                return .pdf417
            case .qr:
                return .qr
            }
        }
    }
}

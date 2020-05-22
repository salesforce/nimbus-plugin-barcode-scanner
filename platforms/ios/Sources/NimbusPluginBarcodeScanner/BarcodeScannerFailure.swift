//
// Copyright (c) 2020, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import Foundation

public struct BarcodeScannerFailure: Codable, Error {
    let code: BarcodeScannerFailureCode
    let message: String
}

public enum BarcodeScannerFailureCode: String, Codable {
    case userDismissedScanner, userDeniedPermission, userDissabledPermission, unknownReason
}

extension BarcodeScannerFailure {
    static let userDismissedScanner = BarcodeScannerFailure(code: .userDismissedScanner,
                                                            message: "The user clicked the button to dismiss the scanner")
    static let userDissabledPermission = BarcodeScannerFailure(code: .userDissabledPermission,
                                                               message: "Permission was disabled by the user and will need to be turned on in settings")
    
    static func unknownReason(_ message: String) -> BarcodeScannerFailure {
        return .init(code: .unknownReason, message: message)
    }
}

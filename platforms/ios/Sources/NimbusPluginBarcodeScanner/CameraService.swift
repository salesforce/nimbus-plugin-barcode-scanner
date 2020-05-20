//
// Copyright (c) 2020, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import Foundation
import AVFoundation

struct CameraService {
            
    var authorizationStatus: AVAuthorizationStatus {
        get {
            return AVCaptureDevice.authorizationStatus(for: .video)
        }
    }
    
    func requestAccess(_ callback: @escaping (BarcodeScannerError?) -> Void) {
        switch self.authorizationStatus {
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { auth in
                if auth {
                    callback(.none)
                } else {
                    callback(.userDeniedPermission)
                }
            }
            
        case .denied, .restricted:
            callback(.userDeniedPermission)

        case .authorized:
            callback(.none)

        @unknown default:
            callback(.unableToUseCamera)
        }
    }
}


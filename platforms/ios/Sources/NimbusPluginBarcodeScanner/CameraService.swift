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
    
    func requestAccess(_ callback: @escaping (Result<Void, BarcodeScannerFailure>) -> Void) {
        switch self.authorizationStatus {
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { auth in
                DispatchQueue.main.async {
                    if auth {
                        callback(.success(()))
                    } else {
                        callback(.failure(.userDissabledPermission))
                    }
                }
            }
            
        case .denied, .restricted:
            callback(.failure(.userDissabledPermission))

        case .authorized:
            callback(.success(()))

        @unknown default:
            callback(.failure(.unknownReason("An unknown access enum was returned.")))
        }
    }
}


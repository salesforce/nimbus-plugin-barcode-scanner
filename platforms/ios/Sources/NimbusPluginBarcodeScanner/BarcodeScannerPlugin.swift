//
// Copyright (c) 2019, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import UIKit
import WebKit
import NimbusBridge

public struct ScannerOptions: Decodable {
    let barcodeTypes: [String]
}

public class BarcodeScannerPlugin {
    weak var currentScannerController: BarcodeScannerViewController?
    var presenter: Presenter

    public typealias Presenter = (UIViewController) -> Void
    
    fileprivate let cameraService = CameraService()

    public init(presenter: @escaping Presenter) {
        self.presenter = presenter
    }
    
    func beginCapture(options: ScannerOptions,
                      callback: @escaping (_ barcode: Barcode?, _ error: BarcodeScannerError?) -> Void) {
        let capture = checkPermissions(callback) { [weak self] in
            guard let strongSelf = self else {
                callback(.none, .unableToUseCamera)
                return
            }
            
            if let existingCaptureController = strongSelf.currentScannerController {
                existingCaptureController.resume()
                return
            }

            let barcodeTypes = options.barcodeTypes

            let captureController = BarcodeScannerViewController(
                targetTypes: barcodeTypes.compactMap {
                    BarcodeType(rawValue: $0)?.metadataObjectType
                })

            if #available(iOS 13, *) {
                captureController.modalPresentationStyle = .fullScreen
            }

            captureController.onCapture = { barcode in
                callback(barcode, nil)
            }
            captureController.onError = { error in
                callback(.none, error)
            }
            
            strongSelf.currentScannerController = captureController
            strongSelf.presenter(captureController)
        }
        
        cameraService.requestAccess(capture)
    }

    func resumeCapture(callback: @escaping (_ barcode: Barcode?, _ error: BarcodeScannerError?) -> Void) {
        let capture = checkPermissions(callback) { [weak self] in
            guard let strongSelf = self else {
                callback(.none, .unableToUseCamera)
                return
            }
            
            strongSelf.currentScannerController?.onCapture = { barcode in
                callback(barcode, nil)
            }
            
            strongSelf.currentScannerController?.onError = { error in
                callback(.none, error)
            }
            
            strongSelf.currentScannerController?.resume()
        }
        
        cameraService.requestAccess(capture)
    }

    func endCapture() {
        currentScannerController?.dismiss(animated: true, completion: nil)
        currentScannerController = nil
    }

}

extension BarcodeScannerPlugin: Plugin {
    public func bind(to webView: WKWebView, bridge: Bridge) {
        let connection = webView.addConnection(to: self, as: "barcodeScanner")
        connection.bind(BarcodeScannerPlugin.beginCapture, as: "beginCapture")
        connection.bind(BarcodeScannerPlugin.resumeCapture, as: "resumeCapture")
        connection.bind(BarcodeScannerPlugin.endCapture, as: "endCapture")
    }

}

//function used to check if permissions exist before presenting the scanner controller
fileprivate func checkPermissions(_ callback: @escaping (Barcode?, BarcodeScannerError?) -> Void,
                                  _ action: @escaping () -> Void)
    -> (BarcodeScannerError?) -> Void {
    return { error in
        if let error = error {
            callback(.none, error)
        } else {
            action()
        }
    }
}

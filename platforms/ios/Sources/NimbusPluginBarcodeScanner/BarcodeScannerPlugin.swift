//
// Copyright (c) 2019, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import UIKit
import WebKit
import Nimbus

public class BarcodeScannerPlugin {
    weak var currentScannerController: BarcodeScannerViewController?
    var presenter: Presenter

    public typealias Presenter = (UIViewController) -> Void

    public init(presenter: @escaping Presenter) {
        self.presenter = presenter
    }
    
    func beginCapture(options: [String: Any],
                      callback: @escaping (_ barcode: Barcode?, _ error: String?) -> Void) {
        if let existingCaptureController = currentScannerController {
            existingCaptureController.resume()
            return
        }

        let barcodeTypes = options["barcodeTypes"] as? [String] ?? []

        let captureController = BarcodeScannerViewController(
            targetTypes: barcodeTypes.compactMap {
                BarcodeType(rawValue: $0)?.metadataObjectType
            })

        captureController.onCapture = { barcode in
            callback(barcode, nil)
        }
        captureController.onError = { error in
            callback(nil, "failed")
        }
        
        currentScannerController = captureController
        presenter(captureController)
    }

    func resumeCapture() {
        currentScannerController?.resume()
    }

    func endCapture() {
        currentScannerController?.dismiss(animated: true, completion: nil)
        currentScannerController = nil
    }

}

extension BarcodeScannerPlugin: NimbusExtension {
    public func bindToWebView(webView: WKWebView) {
        let connection = webView.addConnection(to: self, as: "barcodeScanner")
        connection.bind(BarcodeScannerPlugin.beginCapture, as: "beginCapture")
        connection.bind(BarcodeScannerPlugin.resumeCapture, as: "resumeCapture")
        connection.bind(BarcodeScannerPlugin.endCapture, as: "endCapture")
    }

}


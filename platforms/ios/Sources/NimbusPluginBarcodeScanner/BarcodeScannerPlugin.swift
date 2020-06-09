//
// Copyright (c) 2019, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import UIKit
import WebKit
#if canImport(NimbusBridge)
import NimbusBridge
#elseif canImport(Nimbus)
import Nimbus
#endif

public struct ScannerOptions: Decodable {
    let barcodeTypes: [String]
}

public class BarcodeScannerPlugin {
    weak var currentScannerController: BarcodeScannerViewController?
    var presenter: Presenter

    public typealias Presenter = (UIViewController) -> Void

    public init(presenter: @escaping Presenter) {
        self.presenter = presenter
    }
    
    func beginCapture(options: ScannerOptions,
                      callback: @escaping (_ barcode: Barcode?, _ error: String?) -> Void) {
        if let existingCaptureController = currentScannerController {
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
            callback(nil, "failed")
        }
        
        currentScannerController = captureController
        presenter(captureController)
    }

    func resumeCapture(callback: @escaping (_ barcode: Barcode?, _ error: String?) -> Void) {
        currentScannerController?.onCapture = { barcode in
            callback(barcode, nil)
        }
        currentScannerController?.onError = { error in
            callback(nil, "failed")
        }
        currentScannerController?.resume()
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


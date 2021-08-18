//
//  ViewController.swift
//  BarcodeScanner
//
//  Created by Peter Van Dyk on 8/16/21.
//  Copyright © 2021 Salesforce. All rights reserved.
//

import JavaScriptCore
import UIKit
import WebKit
import Nimbus
import BarcodeScanner

class ViewController: UIViewController {
    var webViewPlugins: [Plugin] = []
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        title = "Nimbus"
    }
    
    lazy var webView = WKWebView(frame: .zero)
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func loadView() {
        view = webView
        
        if let url = Bundle.main.url(forResource: "webview", withExtension: "html") {
            let barcodeScannerPlugin = BarcodeScannerPlugin(presenter: { (scannerVC) in
                self.present(scannerVC, animated: true, completion: nil)
            })
            webViewPlugins.append(barcodeScannerPlugin)
            let webBridge = BridgeBuilder.createBridge(for: webView, plugins: webViewPlugins)
            
            webView.load(URLRequest(url: url))
        }
    }
}

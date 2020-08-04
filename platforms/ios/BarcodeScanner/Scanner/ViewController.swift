//
//  ViewController.swift
//  Scanner
//
//  Created by Paul Tiarks on 8/4/20.
//  Copyright © 2020 Salesforce. All rights reserved.
//

import UIKit
import BarcodeScanner

class ViewController: UIViewController {
    @IBOutlet weak var tableView: UITableView!
    var plugin: BarcodeScannerPlugin?
    var barcodes: [Barcode] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        plugin = BarcodeScannerPlugin(presenter: { (scannerVC) in
            self.present(scannerVC, animated: true, completion: nil)
        })
        let singleScanButton = UIBarButtonItem(title: "Single", style: .plain, target: self, action: #selector(singleScan))
        let multiScanButton = UIBarButtonItem(title: "Multiscan", style: .plain, target: self, action: #selector(multipleScan))
        self.navigationItem.rightBarButtonItem = multiScanButton
        self.navigationItem.leftBarButtonItem = singleScanButton
    }

    @objc func singleScan() {
        let allTypes = BarcodeType.allCases.map { $0.rawValue }
        let options = ScannerOptions(barcodeTypes: allTypes)
        plugin?.beginCapture(options: options, callback: { (barcode, failure) in
            if let code = barcode {
                self.barcodes.append(code)
            }
            self.plugin?.endCapture()
            self.tableView.reloadData()
        })
    }

    @objc func multipleScan() {
        let resume = { (barcode: Barcode?, fail: BarcodeScannerFailure?) in
            if let code = barcode {
                self.barcodes.append(code)
            }
            self.tableView.reloadData()
            self.continueScanning()
        }
        storeAndResume = resume
        let allTypes = BarcodeType.allCases.map { $0.rawValue }
        let options = ScannerOptions(barcodeTypes: allTypes, instructionText: "Scan", successText: "Scanned!")
        plugin?.beginCapture(options: options, callback: resume)
    }

    func continueScanning() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            if let resume = self.storeAndResume {
                self.plugin?.resumeCapture(callback: resume)
            }
        }
    }

    var storeAndResume: ((Barcode?, BarcodeScannerFailure?) -> ())?
}

extension ViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return barcodes.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BarcodeCell", for: indexPath)
        let barcode = barcodes[indexPath.row]
        cell.textLabel?.text = barcode.type.rawValue + " - " + barcode.value
        return cell
    }

}

extension ViewController: UITableViewDelegate {

}


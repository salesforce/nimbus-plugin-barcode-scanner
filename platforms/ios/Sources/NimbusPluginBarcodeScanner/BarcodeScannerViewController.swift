//
// Copyright (c) 2019, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import AVFoundation
import CoreGraphics
import UIKit
import DesignSystem

public class BarcodeScannerViewController: UIViewController {

    public var onCapture: ((Barcode) -> Void)?
    public var onError: ((BarcodeScannerFailure) -> Void)?
    public let instructionText: String?

    public init(targetTypes: [AVMetadataObject.ObjectType] = [], instructionText: String? = nil) {
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        if targetTypes.count > 0 {
            self.targetTypes = targetTypes
        } else {
            self.targetTypes = BarcodeType.allCases.map { $0.metadataObjectType }
        }
        self.instructionText = instructionText
        self.statusBar = ScannerStatusBar(instructionText: instructionText)
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        setupView()
        buildCapture()
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

#if targetEnvironment(simulator)
#else
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
#endif
    }

    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if let overlay = overlayView {
            overlay.layer.mask = createReticleLayer(overlay: overlay, verticalOffset: 0.0)
        }
        view.bringSubviewToFront(statusBar)
    }

    override public func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        if captureSession.isRunning {
            captureSession.stopRunning()
        }
    }
    
    fileprivate func setupView() {
        view.backgroundColor = .white
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        view.addSubview(statusBar)

        let overlay = createOverlayView()
        view.addSubview(overlay)
        setupOverlayConstraints(overlay: overlay)
        overlayView = overlay
    }
    
    fileprivate func buildCapture() {
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            onError?(.unknownReason("Unable to create video capture device."))
            return
        }
        
        let videoInput: AVCaptureDeviceInput

        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            onError?(.unknownReason("Failed to create capture input: \(error)"))
            return
        }

        guard captureSession.canAddInput(videoInput) else {
            onError?(.unknownReason("Unable to add input to capture session."))
            return
        }
        
        captureSession.addInput(videoInput)

        let metadataOutput = AVCaptureMetadataOutput()

        guard captureSession.canAddOutput(metadataOutput) else {
            onError?(.unknownReason("unable to add metadata output to capture session."))
            return
        }
        
        captureSession.addOutput(metadataOutput)

        metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
        metadataOutput.metadataObjectTypes = targetTypes.isEmpty ? metadataOutput.availableMetadataObjectTypes : targetTypes
    }

    private func createOverlayView() -> UIView {
        let overlay = UIView()
        overlay.translatesAutoresizingMaskIntoConstraints = false
        overlay.backgroundColor = UIColor(red: 0.03, green: 0.03, blue: 0.03, alpha: 0.5)
        overlay.clipsToBounds = true
        return overlay
    }

    private func setupOverlayConstraints(overlay: UIView) {
        let views = ["overlay": overlay, "status": statusBar]
        let horizontal = NSLayoutConstraint.constraints(withVisualFormat: "H:|[overlay]|", metrics: nil, views: views)
        let vertical = NSLayoutConstraint.constraints(withVisualFormat: "V:|[overlay]|", metrics: nil, views: views)
        let top = (overlay.frame.height / 2) + 168.0
        let statusVertical = NSLayoutConstraint(item: statusBar, attribute: .top, relatedBy: .equal, toItem: overlay, attribute: .centerY, multiplier: 1.0, constant: top)
        let statusHorizontal = NSLayoutConstraint(item: statusBar, attribute: .centerX, relatedBy: .equal, toItem: overlay, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        view.addConstraints(horizontal)
        view.addConstraints(vertical)
        view.addConstraints([statusVertical, statusHorizontal])
    }

    private func createReticleLayer(overlay: UIView, verticalOffset: CGFloat) -> CAShapeLayer {
        let shape = CAShapeLayer()
        let path = CGMutablePath()
        let width: CGFloat = 304.0
        let height: CGFloat = 304.0
        let x: CGFloat = overlay.frame.midX - (width / 2)
        let y: CGFloat = (overlay.frame.midY - (height / 2)) - verticalOffset
        path.addRoundedRect(in: CGRect(x: x, y: y, width: width, height: height), cornerWidth: 4, cornerHeight: 4)
        path.addRect(CGRect(origin: .zero, size: overlay.frame.size))
        shape.path = path
        shape.backgroundColor = UIColor.black.cgColor
        shape.fillRule = .evenOdd

        return shape
    }

    @IBAction func cancelCaptureSession(sender: UIBarButtonItem) {
        onError?(.userDismissedScanner)
        self.dismiss(animated: true, completion: nil)
    }

    public func resume() {
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
    }

    private var overlayView: UIView?
    private let statusBar: ScannerStatusBar
    private let captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer
    private let targetTypes: [AVMetadataObject.ObjectType]
}

extension BarcodeScannerViewController: AVCaptureMetadataOutputObjectsDelegate {

    public func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let barcode = Barcode(machineReadableCode: readableObject) else { return }
            captureSession.stopRunning()
            AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            onCapture?(barcode)
        }
    }
}

private class ScannerStatusBar: UIView {
    init(instructionText: String?) {
        label = UILabel(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        label.translatesAutoresizingMaskIntoConstraints = false
        label.backgroundColor = UIColor.clear
        label.textColor = UIColor.white
        label.font = UIFont.systemFont(ofSize: 16)
        label.isEnabled = true
        label.textAlignment = .center
        label.numberOfLines = 1
        label.lineBreakMode = .byTruncatingTail
        label.text = instructionText
        label.isUserInteractionEnabled = true

        super.init(frame: CGRect(x: 0, y: 0, width: 304, height: 50))
        self.addSubview(label)
        self.translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = UIColor(red: 0.03, green: 0.03, blue: 0.03, alpha: 1.00)
        let width = NSLayoutConstraint(item: self, attribute: .width, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1.0, constant: 304.0)
        let height = NSLayoutConstraint(item: self, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1.0, constant: 50.0)
        let labelX = NSLayoutConstraint(item: label, attribute: .centerX, relatedBy: .equal, toItem: self, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        let labelY = NSLayoutConstraint(item: label, attribute: .centerY, relatedBy: .equal, toItem: self, attribute: .centerY, multiplier: 1.0, constant: 0.0)
        self.addConstraints([width, height, labelX, labelY])
        self.layer.cornerRadius = 4
        if instructionText == nil {
            self.isHidden = true
        }
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private let label: UILabel
}

//
// Copyright (c) 2019, Salesforce.com, inc.
// All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
//

import AVFoundation
import CoreGraphics
import UIKit

public enum BarcodeType: String, Codable {
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

public class BarcodeScannerViewController: UIViewController {

    public var onCapture: ((Barcode) -> Void)?
    public var onError: ((Error) -> Void)?

    public init(targetTypes: [AVMetadataObject.ObjectType] = []) {
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        self.targetTypes = targetTypes
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)

        // add toolbar
        let toolbarView = UIToolbar(frame: CGRect(origin: .zero, size: CGSize(width: view.bounds.size.width, height: 44)))
        toolbar = toolbarView
        toolbarView.translatesAutoresizingMaskIntoConstraints = false
        toolbarView.barStyle = .blackTranslucent
        toolbarView.isTranslucent = true
        view.addSubview(toolbarView)
        setupToolbarConstraints(toolbar: toolbarView)

        let overlay = createOverlayView()
        view.addSubview(overlay)
        setupOverlayConstraints(overlay: overlay)
        overlayView = overlay

        // cancel button
        let cancelButton = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(cancelCaptureSession(sender:)))
        toolbarView.items = [cancelButton, UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil)]

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            return
        }
        let videoInput: AVCaptureDeviceInput

        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            onError?(error)
            return
        }

        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            return
        }

        let metadataOutput = AVCaptureMetadataOutput()

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            if targetTypes.isEmpty {
                metadataOutput.metadataObjectTypes = metadataOutput.availableMetadataObjectTypes
            } else {
                metadataOutput.metadataObjectTypes = targetTypes
            }
        } else {
            return
        }
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
            overlay.layer.mask = createReticleLayer(overlay: overlay)
        }
    }

    override public func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        if captureSession.isRunning {
            captureSession.stopRunning()
        }
    }

    private func createOverlayView() -> UIView {
        let overlay = UIView()
        overlay.translatesAutoresizingMaskIntoConstraints = false
        overlay.backgroundColor = UIColor.black.withAlphaComponent(0.8)
        overlay.clipsToBounds = true
        return overlay
    }

    private func setupToolbarConstraints(toolbar: UIToolbar) {
        var constraints: [NSLayoutConstraint] = []
        constraints.append(NSLayoutConstraint(item: toolbar, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1.0, constant: 44.0))
        constraints.append(NSLayoutConstraint(item: toolbar, attribute: .width, relatedBy: .equal, toItem: view, attribute: .width, multiplier: 1.0, constant: 0.0))
        let guide = view.safeAreaLayoutGuide
        constraints.append(toolbar.topAnchor.constraint(equalToSystemSpacingBelow: guide.topAnchor, multiplier: 1.0))
        view.addConstraints(constraints)
    }

    private func setupOverlayConstraints(overlay: UIView) {
        guard let toolbarView = toolbar else { return }
        let views = ["overlay": overlay, "toolbar": toolbarView]
        let horizontal = NSLayoutConstraint.constraints(withVisualFormat: "H:|[overlay]|", metrics: nil, views: views)
        let vertical = NSLayoutConstraint.constraints(withVisualFormat: "V:[toolbar][overlay]|", metrics: nil, views: views)
        view.addConstraints(horizontal)
        view.addConstraints(vertical)
    }

    private func createReticleLayer(overlay: UIView) -> CAShapeLayer {
        let shape = CAShapeLayer()
        let path = CGMutablePath()
        let width = overlay.frame.width - 60.0
        let height: CGFloat = 250.0
        path.addRoundedRect(in: CGRect(x: overlay.frame.midX - (width / 2), y: 150.0, width: width, height: height), cornerWidth: 8, cornerHeight: 8)
        path.addRect(CGRect(origin: .zero, size: overlay.frame.size))
        shape.path = path
        shape.backgroundColor = UIColor.black.cgColor
        shape.fillRule = .evenOdd
        return shape
    }

    @IBAction func cancelCaptureSession(sender: UIBarButtonItem) {
        self.dismiss(animated: true, completion: nil)
    }

    public func resume() {
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
    }

    private var toolbar: UIToolbar?
    private var overlayView: UIView?
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

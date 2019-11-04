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
        view.backgroundColor = .black
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)

//        // add capture button
//        let captureButton = UIButton(frame: .zero)
//        captureButton.translatesAutoresizingMaskIntoConstraints = false
//        captureButton.setTitle("Capture", for: .normal)
//        captureButton.addTarget(self, action: #selector(capture(sender:)), for: .touchUpInside)
//
//        view.addSubview(captureButton)
//        view.bottomAnchor.constraint(equalTo: captureButton.bottomAnchor).isActive = true
//        view.centerXAnchor.constraint(equalTo: captureButton.centerXAnchor).isActive = true
//        captureButton.sizeToFit()

//        // add target rectangle
//        let target = UIView(frame: .zero)
//        target.translatesAutoresizingMaskIntoConstraints = false
//        target.backgroundColor = .clear
//        target.layer.borderColor = UIColor.white.withAlphaComponent(0.6).cgColor
//        target.layer.borderWidth = 2
//        view.addSubview(target)
//        target.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.5).isActive = true
//        target.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
//        target.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
//        target.heightAnchor.constraint(equalTo: target.widthAnchor, multiplier: 1.667).isActive = true

        // add toolbar
        let toolbar = UIToolbar(frame: CGRect(origin: .zero, size: CGSize(width: view.bounds.size.width, height: 44)))
        toolbar.barStyle = .blackTranslucent
        toolbar.isTranslucent = true
        view.addSubview(toolbar)

        // cancel button
        let cancelButton = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(cancelCaptureSession(sender:)))
        toolbar.items = [cancelButton, UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil)]

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
//            failed()
            return
        }
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if !captureSession.isRunning {
            captureSession.startRunning()
        }
    }

    override public func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        if captureSession.isRunning {
            captureSession.stopRunning()
        }
    }

    @IBAction func cancelCaptureSession(sender: UIBarButtonItem) {
        self.dismiss(animated: true, completion: nil)
    }

    @IBAction func capture(sender: UIControl) {

//        let videoPreviewLayerOrientation = previewLayer.connection?.videoOrientation
//        if let photoOutputConnection = photoOutput.connection(with: .video) {
//            photoOutputConnection.videoOrientation = videoPreviewLayerOrientation!
//        }
//        let photoSettings = AVCapturePhotoSettings()
//        photoSettings.isHighResolutionPhotoEnabled = true
//        if !photoSettings.__availablePreviewPhotoPixelFormatTypes.isEmpty {
//            photoSettings.previewPhotoFormat = [kCVPixelBufferPixelFormatTypeKey as String: photoSettings.__availablePreviewPhotoPixelFormatTypes.first!]
//        }
//        self.photoOutput.capturePhoto(with: photoSettings, delegate: self)
    }

    public func resume() {
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
    }

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

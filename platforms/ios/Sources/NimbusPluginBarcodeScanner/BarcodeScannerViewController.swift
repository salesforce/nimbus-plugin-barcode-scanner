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
    public let successText: String?

    public init(targetTypes: [AVMetadataObject.ObjectType] = [], instructionText: String? = nil, successText: String? = nil) {
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        if targetTypes.count > 0 {
            self.targetTypes = targetTypes
        } else {
            self.targetTypes = BarcodeType.allCases.map { $0.metadataObjectType }
        }
        self.instructionText = instructionText
        self.successText = successText
        self.statusBar = ScannerStatusBar(instructionText: instructionText)
        let xImage = UIImage.sldsActionIcon(.remove, with: .white, andBGColor: .clear, andSize: 45.0)
        closeButton = UIButton(type: .custom)
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        closeButton.setBackgroundImage(xImage, for: .normal)
        closeButton.sizeToFit()
        closeButton.accessibilityLabel = "Close"
        let checkImage = UIImage.sldsActionIcon(.check
        , with: .white, andBGColor: UIColor(red: 0.02, green: 0.52, blue: 0.29, alpha: 1.00), andSize: 55.0)
        successIcon = UIImageView(image: checkImage)
        successIcon.translatesAutoresizingMaskIntoConstraints = false
        successIcon.sizeToFit()
        successIcon.isHidden = true
        successBorder = UIView()
        successBorder.translatesAutoresizingMaskIntoConstraints = false
        successBorder.layer.cornerRadius = 4.0
        successBorder.backgroundColor = UIColor(red: 0.08, green: 0.54, blue: 0.93, alpha: 1.00)
        successBorder.isHidden = true
        super.init(nibName: nil, bundle: nil)
        closeButton.addTarget(self, action: #selector(cancelCaptureSession(sender:)), for: .touchUpInside)
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
        view.addSubview(closeButton)
        view.addSubview(successIcon)
        overlay.addSubview(successBorder)
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

        let successBorderMargin = margin - 5
        let borderWidth = NSLayoutConstraint(item: successBorder, attribute: .width, relatedBy: .equal, toItem: overlay, attribute: .width, multiplier: 1.0, constant: ((successBorderMargin * 2) * -1))
        let borderHeight = NSLayoutConstraint(item: successBorder, attribute: .height, relatedBy: .equal, toItem: successBorder, attribute: .width, multiplier: 1.0, constant: 0.0)
        let borderX = NSLayoutConstraint(item: successBorder, attribute: .centerX, relatedBy: .equal, toItem: overlay, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        let borderY = NSLayoutConstraint(item: successBorder, attribute: .centerY, relatedBy: .equal, toItem: overlay, attribute: .centerY, multiplier: 1.0, constant: 0.0)
        overlay.addConstraints([borderWidth, borderHeight, borderX, borderY])

        let statusVertical = NSLayoutConstraint(item: statusBar, attribute: .top, relatedBy: .equal, toItem: successBorder, attribute: .bottom, multiplier: 1.0, constant: margin / 2)
        let statusHorizontal = NSLayoutConstraint(item: statusBar, attribute: .centerX, relatedBy: .equal, toItem: overlay, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        let statusLeft = NSLayoutConstraint(item: statusBar, attribute: .left, relatedBy: .equal, toItem: overlay, attribute: .left, multiplier: 1.0, constant: margin)
        let statusRight = NSLayoutConstraint(item: statusBar, attribute: .right, relatedBy: .equal, toItem: overlay, attribute: .right, multiplier: 1.0, constant: (margin * -1))
        view.addConstraints(horizontal)
        view.addConstraints(vertical)
        view.addConstraints([statusVertical, statusHorizontal, statusLeft, statusRight])

        let closeX = NSLayoutConstraint(item: closeButton, attribute: .right, relatedBy: .equal, toItem: overlay, attribute: .right, multiplier: 1.0, constant: -12.0)
        let closeY = NSLayoutConstraint(item: closeButton, attribute: .top, relatedBy: .equal, toItem: overlay, attribute: .top, multiplier: 1.0, constant: 44.0)
        view.addConstraints([closeX, closeY])

        let checkX = NSLayoutConstraint(item: successIcon, attribute: .centerX, relatedBy: .equal, toItem: statusBar, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        let checkY = NSLayoutConstraint(item: successIcon, attribute: .top, relatedBy: .equal, toItem: statusBar, attribute: .bottom, multiplier: 1.0, constant: 30.0)
        view.addConstraints([checkX, checkY])
    }

    private func createReticleLayer(overlay: UIView, verticalOffset: CGFloat) -> CAShapeLayer {
        let shape = CAShapeLayer()
        let path = CGMutablePath()
        let width: CGFloat = overlay.frame.width - (margin * 2)
        let height: CGFloat = width
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
        configureForScan()
        if !captureSession.isRunning {
            captureSession.startRunning()
        }
    }

    func configureForSuccess() {
        statusBar.configureText(text: successText)
        successIcon.isHidden = false
        successBorder.isHidden = false
    }

    func configureForScan() {
        statusBar.configureText(text: instructionText)
        successIcon.isHidden = true
        successBorder.isHidden = true
    }

    private var overlayView: UIView?
    private let statusBar: ScannerStatusBar
    private let closeButton: UIButton
    private let successIcon: UIImageView
    private let successBorder: UIView
    private let captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer
    private let targetTypes: [AVMetadataObject.ObjectType]
    private let margin: CGFloat = 36.0
}

extension BarcodeScannerViewController: AVCaptureMetadataOutputObjectsDelegate {

    public func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let barcode = Barcode(machineReadableCode: readableObject) else { return }
            captureSession.stopRunning()
            configureForSuccess()
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
        let height = NSLayoutConstraint(item: self, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1.0, constant: 50.0)
        let labelX = NSLayoutConstraint(item: label, attribute: .centerX, relatedBy: .equal, toItem: self, attribute: .centerX, multiplier: 1.0, constant: 0.0)
        let labelY = NSLayoutConstraint(item: label, attribute: .centerY, relatedBy: .equal, toItem: self, attribute: .centerY, multiplier: 1.0, constant: 0.0)
        let labelWidth = NSLayoutConstraint(item: label, attribute: .width, relatedBy: .equal, toItem: self, attribute: .width, multiplier: 1.0, constant: 0.0)
        let labelHeight = NSLayoutConstraint(item: label, attribute: .height, relatedBy: .equal, toItem: self, attribute: .height, multiplier: 1.0, constant: 0.0)
        self.addConstraints([height, labelX, labelY, labelWidth, labelHeight])
        self.layer.cornerRadius = 4
        if instructionText == nil {
            self.isHidden = true
        }
    }

    func configureText(text: String?) {
        label.text = text
        label.isHidden = text == nil
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private let label: UILabel
}

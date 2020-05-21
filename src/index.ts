/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

export interface Barcode {
  type: BarcodeType;
  value: string;
}

export enum BarcodeType {
  CODE_128 = "code128",
  CODE_39 = "code39",
  CODE_93 = "code93",
  DATA_MATRIX = "datamatrix",
  EAN_13 = "ean13",
  EAN_8 = "ean8",
  ITF = "itf",
  UPC_E = "upce",
  PDF_417 = "pdf417",
  QR = "qr",
}

export interface BarcodeScannerFailure {
  code: BarcodeScannerFailureCode;
  message: string;
}

export enum BarcodeScannerFailureCode {
  //the user clicked the button to dismiss the scanner
  USER_DISMISSED_SCANNER = "userDismissedScanner",

  //this is only ever returned on android
  //android: permission was denied by user when prompt, could ask again
  USER_DENIED_PERMISSION = "userDeniedPermission",

  //both ios and android will use this as it requires the same action of the user going to settings
  //android: permission was denied along "don't ask again" when prompt, will need to go app setting to turn on
  //ios: permission was disabled by the user and will need to be turned on in settings
  USER_DISABLED_PERMISSION = "userDisabledPermissions",

  //A hardware or unknown failure happened when trying to use the camera
  //or other reason, like FirebaseVision failure.
  //This is not caused by a lack of permission.
  UNKNOWN_REASON = "unknownReason",

  // android only: the hosting activity could be destroyed while scanning is in
  // foreground, as a result the success or failure can't delivered to webview.
  // It could be delivered to hosting activity when recreated after
  // leaving the scanning activity, but not the webview
  BRIDGE_UNAVAILABLE ="bridgeUnavailable"
}

export interface BarcodeScannerOptions {
  barcodeTypes: BarcodeType[];
}

export interface BarcodeScanner {
  // Begin a capture session with the specified options
  beginCapture(
    options: BarcodeScannerOptions,
    callback: (barcode: Barcode, error: BarcodeScannerFailure) => void
  ): void;

  // Resume an existing scanning session using options from beginCapture
  resumeCapture(callback: (barcode: Barcode, error: BarcodeScannerFailure) => void): void;

  // End a capture session and dismiss the scanner
  endCapture(): void;
}

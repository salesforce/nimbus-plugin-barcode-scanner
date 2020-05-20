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

export enum BarcodeScannerError {
  //the user clicked the button to dismiss the scanner
  USER_DISMISSED_SCANNER = 0,

  //ios: permission was disabled by the user and will need to be turned on in settings
  //android: permission was denied by user when prompt, could ask again
  USER_DENIED_PERMISSION = 1,

  //android: permission was denied along "don't ask again" when prompt, will need to go app setting to turn on
  USER_DISABLED_PERMISSION = 2,

  //some sort of error happened when trying to use/open the camera not caused by permissions
  UNABLE_TO_USE_CAMERA = 3,

  // android only: the hosting activity could be destroyed while scanning is in
  // foreground, as a result the success or failure can't delivered to webview.
  // It could be delivered to hosting activity when recreated after
  // leaving the scanning activity, but not the webview
  BRIDGE_UNAVAILABLE = 4
}

export interface BarcodeScannerOptions {
  barcodeTypes: BarcodeType[];
}

export interface BarcodeScanner {
  // Begin a capture session with the specified options
  beginCapture(
    options: BarcodeScannerOptions,
    callback: (barcode: Barcode, error: BarcodeScannerError) => void
  ): void;

  // Resume an existing scanning session using options from beginCapture
  resumeCapture(callback: (barcode: Barcode, error: BarcodeScannerError) => void): void;

  // End a capture session and dismiss the scanner
  endCapture(): void;
}

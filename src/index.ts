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

export interface BarcodeScannerOptions {
  barcodeTypes: BarcodeType[];
}

export interface BarcodeScanner {
  // Begin a capture session with the specified options
  beginCapture(
    options: BarcodeScannerOptions,
    callback: (barcode: Barcode, error: String) => void
  ): void;

  // Resume an existing scanning session using options from beginCapture
  resumeCapture(callback: (barcode: Barcode, error: String) => void): void;

  // End a capture session and dismiss the scanner
  endCapture(): void;
}

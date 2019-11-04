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
  QR = "qr"
}

export interface BarcodeScannerOptions {
  barcodeTypes: BarcodeType[];
}

export interface BarcodeScanner {
  // Begin a capture session with the specified options
  beginCapture(
    options: BarcodeScannerOptions,
    onScan: (barcode: Barcode) => void,
    onError: (error: Error) => void
  ): void;

  // Resume an existing scanning session
  resumeCapture(): void;

  // End a capture session and dismiss the scanner
  endCapture(): void;
}

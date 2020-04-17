let barcodeScanner = __nimbus.plugins.barcodeScanner;
function beginScanning() {
    if(typeof barcodeScanner !== undefined && barcodeScanner !== null){
        document.querySelector('#beginScanning').style.visibility = 'hidden'
        document.querySelector('#resumeScanning').style.visibility = 'visible'
        document.querySelector('#endScanning').style.visibility = 'visible'
        barcodeScanner.beginCapture(
            {"barcodeTypes": ["code128", "code39", "code93", "ean13", "ean8", "upce", "upca", "qr"]},
            (barcode, error) => {
                if (barcode !== null) {
                    document.querySelector("#result").textContent = "type: " + barcode.type + " value: " + decodeURIComponent(barcode.value);
                } if (error !== null) {
                    document.querySelector("#result").textContent = "error: " + error;
                }
            }
        );
    }
}

function endScanning() {
    if(typeof barcodeScanner !== undefined && barcodeScanner !== null){
        document.querySelector('#beginScanning').style.visibility = 'visible'
        document.querySelector('#resumeScanning').style.visibility = 'hidden'
        document.querySelector('#endScanning').style.visibility = 'hidden'
        barcodeScanner.endCapture();
    }
}

function resumeScanning() {
    if(typeof barcodeScanner !== undefined && barcodeScanner !== null){
        barcodeScanner.resumeCapture();
    }
}

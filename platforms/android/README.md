### Android Plugin

Nimbus Plugin for scanning barcodes

Contains 2 projects

## barcodescanner

This project contains the logic and activity for scanning barcodes, as well as the BarcodeScannerPlugin to wire up to your consumer app's webview.

Before running this plugin, [add Firebase to your Android project](https://firebase.google.com/docs/android/setup).

## demoapp

This project contains a sample application for consuming the barcode scanner plugin. See the MainActivity for wiring up the plugin to the WebView, and the webview.html for wiring calling the plugin.

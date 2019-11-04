Pod::Spec.new do |s|
  s.name             = 'NimbusPluginBarcodeScanner'
  s.version          = '0.1.0'
  s.summary          = 'A Nimbus plugin for scanning barcodes.'

  s.description      = <<-DESC
  A plugin for the Nimbus framework to scan barcodes from a webview and return results.
                       DESC

  s.homepage         = 'https://github.com/salesforce/nimbus-plugin-barcode-scanner'
  s.license          = 'BSD-3-Clause'
  s.author           = { 'Hybrid Platform Team' => 'hybridplatform@salesforce.com' }
  s.source           = { :git => 'https://github.com/salesforce/nimbus-plugin-barcode-scanner.git', :tag => s.version.to_s }

  s.source_files     = 'platforms/iOS/Sources/**/*.swift'
  s.swift_version    = '5.0'

  s.ios.deployment_target = '11.0'
  s.dependency 'Nimbus'
end

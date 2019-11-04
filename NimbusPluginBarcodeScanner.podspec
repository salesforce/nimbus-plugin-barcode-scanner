Pod::Spec.new do |s|
  s.name             = 'NimbusPluginBarcodeScanner'
  s.version          = '0.1.0'
  s.summary          = 'A short description of NimbusPluginBarcodeScanner.'

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://git.soma.salesforce.com/hype/nimbus-plugin-barcode-scanner'
  s.license          = 'BSD-3-Clause'
  s.author           = { 'Hybrid Platform Team' => 'hybridplatform@salesforce.com' }
  s.source           = { :git => 'https://git.soma.salesforce.com/hype/nimbus-plugin-barcode-scanner.git', :tag => s.version.to_s }

  s.source_files     = 'platforms/iOS/Sources/**/*.swift'
  s.swift_version    = '5.0'

  s.ios.deployment_target = '11.0'
  s.dependency 'Nimbus'
end

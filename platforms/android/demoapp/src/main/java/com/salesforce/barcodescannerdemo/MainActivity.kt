/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerdemo

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.salesforce.barcodescannerplugin.BarcodeScannerPlugin
import com.salesforce.barcodescannerplugin.webViewBinder
import com.salesforce.nimbus.BoundPlugin
import com.salesforce.nimbus.NimbusJSUtilities
import com.salesforce.nimbus.bridge.webview.WebViewBridge
import com.salesforce.nimbus.bridge.webview.bridge
import com.salesforce.nimbus.core.plugins.DeviceInfoPlugin
import kotlinx.android.synthetic.main.activity_main.plugin_webview
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    @BoundPlugin
    private lateinit var barcodeScannerPlugin: BarcodeScannerPlugin
    @BoundPlugin
    private lateinit var deviceInfoPlugin: DeviceInfoPlugin

    private lateinit var webViewBridge: WebViewBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // initial the demo webview html content
        initializeDemoWebViewHtmlContent()

        // register the plugins with the webview in the nimbus bridge
        barcodeScannerPlugin = BarcodeScannerPlugin(this)
        deviceInfoPlugin = DeviceInfoPlugin(this)

        webViewBridge = plugin_webview.bridge {
            bind { deviceInfoPlugin.webViewBinder() }
            bind { barcodeScannerPlugin.webViewBinder() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewBridge.detach()
    }

    private fun initializeDemoWebViewHtmlContent() {
        WebView.setWebContentsDebuggingEnabled(true)

        val sourceHtml = this.resources.assets.open("webview.html")
        val htmlStream = NimbusJSUtilities.injectedNimbusStream(sourceHtml.buffered(), this)
        val html = htmlStream.bufferedReader(StandardCharsets.UTF_8).readText()
        plugin_webview.loadDataWithBaseURL("", html, "text/html", StandardCharsets.UTF_8.name(), "")
    }
}

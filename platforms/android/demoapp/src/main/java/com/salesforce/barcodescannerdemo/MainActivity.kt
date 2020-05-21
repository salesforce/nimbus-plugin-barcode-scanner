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
import com.salesforce.barcodescannerplugin.BarcodeScannerPluginBinder
import com.salesforce.nimbus.Bridge
import com.salesforce.nimbus.plugins.DeviceInfoPlugin
import com.salesforce.nimbus.plugins.DeviceInfoPluginBinder
import com.salesforce.nimbusjs.NimbusJSUtilities
import kotlinx.android.synthetic.main.activity_main.plugin_webview
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var barcodeScannerPlugin: BarcodeScannerPlugin
    private val nimbusBridge = Bridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // initial the demo webview html content
        initializeDemoWebViewHtmlContent()

        // register the plugins with the webview in the nimbus bridge
        barcodeScannerPlugin = BarcodeScannerPlugin(this)
        nimbusBridge.add(BarcodeScannerPluginBinder(barcodeScannerPlugin))

        nimbusBridge.add(DeviceInfoPluginBinder(DeviceInfoPlugin(this)))

        nimbusBridge.attach(plugin_webview)
    }

    override fun onDestroy() {
        super.onDestroy()
        nimbusBridge.detach()
    }

    private fun initializeDemoWebViewHtmlContent() {
        WebView.setWebContentsDebuggingEnabled(true)

        val sourceHtml = this.resources.assets.open("webview.html")
        val htmlStream = NimbusJSUtilities.injectedNimbusStream(sourceHtml.buffered(), this)
        val html = htmlStream.bufferedReader(StandardCharsets.UTF_8).readText()
        plugin_webview.loadDataWithBaseURL("", html, "text/html", StandardCharsets.UTF_8.name(), "")
    }
}

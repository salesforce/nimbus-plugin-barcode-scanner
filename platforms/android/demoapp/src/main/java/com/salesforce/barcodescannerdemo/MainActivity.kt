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
import com.salesforce.nimbus.NimbusBridge
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val nimbusBridge = NimbusBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nimbusBridge.add(BarcodeScannerPluginBinder(BarcodeScannerPlugin(this)))
        nimbusBridge.attach(plugin_webview)
        plugin_webview.loadUrl("file:///android_asset/webview.html")
        plugin_webview.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        nimbusBridge.detach()
    }
}

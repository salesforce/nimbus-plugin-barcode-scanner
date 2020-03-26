/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import org.greenrobot.eventbus.EventBus

/** Utility class to provide helper methods.  */
object Utils {

    private const val REQUIRED_PERMISSION = "android.permission.CAMERA"

    fun arePermissionsGranted(activity: Activity): Boolean {
        return (checkSelfPermission(
            activity,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(REQUIRED_PERMISSION), /* requestCode= */ 0
        )
    }

    fun postError(sourceClass: String, errorMessage: String, error: Exception) {
        Log.e(sourceClass, errorMessage, error)
        EventBus.getDefault().postSticky(
            BarcodeErrorEvent(
                "$errorMessage: ${error.message}"
            )
        )
    }
}

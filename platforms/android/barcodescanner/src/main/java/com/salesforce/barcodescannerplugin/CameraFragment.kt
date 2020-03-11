package com.salesforce.barcodescannerplugin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private lateinit var cameraView: TextureView
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView
    private fun startCamera() {
        // TODO: Implement CameraX operations
    }

    private fun updateTransform() {
        // TODO: Implement camera viewfinder transformations
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.barcode_plugin_activity, container, false)

        cameraView = view.findViewById(R.id.preview_view)
        viewFinder.post { startCamera() }
        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
        return view
    }
}
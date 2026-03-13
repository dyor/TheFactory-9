package org.example.project.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.UIKit.UIView
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(modifier: Modifier) {
    val cameraSession = remember {
        val session = AVCaptureSession()
        
        val devices = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
        var frontCamera: AVCaptureDevice? = null
        for (device in devices) {
            val captureDevice = device as? AVCaptureDevice
            if (captureDevice?.position == AVCaptureDevicePositionFront) {
                frontCamera = captureDevice
                break
            }
        }
        
        if (frontCamera == null) {
            frontCamera = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        }

        if (frontCamera != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(frontCamera, null)
            if (input != null && session.canAddInput(input as platform.AVFoundation.AVCaptureInput)) {
                session.addInput(input as platform.AVFoundation.AVCaptureInput)
            }
        }
        session
    }

    UIKitView(
        factory = {
            val cameraContainer = UIView()
            val cameraLayer = AVCaptureVideoPreviewLayer(session = cameraSession)
            cameraLayer.videoGravity = platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
            cameraContainer.layer.addSublayer(cameraLayer)
            cameraSession.startRunning()
            cameraContainer
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.sublayers?.firstOrNull()?.setFrame(view.layer.bounds)
            CATransaction.commit()
        }
    )
}

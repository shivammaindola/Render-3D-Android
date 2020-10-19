package com.google.android.filament.gltf


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import com.google.android.filament.Skybox
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class SampleActivity : Activity() {

        companion object {
                init {
                        Utils.init()
                }
        }

        private lateinit var surfaceView: SurfaceView
        private lateinit var choreographer: Choreographer
        private lateinit var modelViewer: ModelViewer

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                surfaceView = SurfaceView(this).apply { setContentView(this) }
                choreographer = Choreographer.getInstance()
                modelViewer = ModelViewer(surfaceView)
                surfaceView.setOnTouchListener(modelViewer)

//                loadGlb("DamagedHelmet")
//                modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)

                loadGltf("scene")
                loadEnvironment("venetian_crossroads_2k")
        }

        private fun loadEnvironment(ibl: String) {
                // Create the indirect light source and add it to the scene.
                var buffer = readAsset("envs/$ibl/${ibl}_ibl.ktx")
                KtxLoader.createIndirectLight(modelViewer.engine, buffer).apply {
                        intensity = 50_000f
                        modelViewer.scene.indirectLight = this
                }

                // Create the sky box and add it to the scene.
                buffer = readAsset("envs/$ibl/${ibl}_skybox.ktx")
                KtxLoader.createSkybox(modelViewer.engine, buffer).apply {
                        modelViewer.scene.skybox = this
                }
        }

        private fun loadGltf(name: String) {
                val buffer = readAsset("models/${name}.gltf")
                modelViewer.loadModelGltf(buffer) { uri -> readAsset("models/$uri") }
                modelViewer.transformToUnitCube()
        }

        private fun readAsset(assetName: String): ByteBuffer {
                val input = assets.open(assetName)
                val bytes = ByteArray(input.available())
                input.read(bytes)
                return ByteBuffer.wrap(bytes)
        }

        private val frameCallback = object : Choreographer.FrameCallback {
                private val startTime = System.nanoTime()
                override fun doFrame(currentTime: Long) {
                        val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
                        choreographer.postFrameCallback(this)
                        modelViewer.animator?.apply {
                                if (animationCount > 0) {
                                        applyAnimation(0, seconds.toFloat())
                                }
                                updateBoneMatrices()
                        }
                        modelViewer.render(currentTime)
                }
        }

        override fun onResume() {
                super.onResume()
                choreographer.postFrameCallback(frameCallback)
        }

        override fun onPause() {
                super.onPause()
                choreographer.removeFrameCallback(frameCallback)
        }

        override fun onDestroy() {
                super.onDestroy()
                choreographer.removeFrameCallback(frameCallback)
        }
}


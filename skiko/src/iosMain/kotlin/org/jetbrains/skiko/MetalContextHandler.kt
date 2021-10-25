package org.jetbrains.skiko

import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.useContents
import org.jetbrains.skia.*
import platform.CoreGraphics.CGSizeMake
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.QuartzCore.CAMetalDrawableProtocol
import kotlin.system.getTimeNanos

internal class MetalContextHandler(private val layer: SkiaLayer) {
    var context: DirectContext? = null
    var renderTarget: BackendRenderTarget? = null
    var surface: Surface? = null
    var canvas: Canvas? = null

    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice()!!
    private val queue = device.newCommandQueue()!!
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer()

    init {
        metalLayer.init(this.layer, device)
    }

    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!!

    fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    private var currentWidth = 0
    private var currentHeight = 0

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        if (layer.isShowing()) {
            layer.update(getTimeNanos())
            draw()
        }
    }

    fun disposeCanvas() {
        surface?.close()
        renderTarget?.close()
    }

    fun dispose() {
        if (!isDisposed) {
            frameDispatcher.cancel()
            metalLayer.dispose()
            isDisposed = true
        }
    }

    fun initCanvas() {
        disposeCanvas()
        val scale = layer.contentScale
        val (w, h) = layer.view!!.frame.useContents {
            (size.width * scale).toInt().coerceAtLeast(0) to (size.height * scale).toInt().coerceAtLeast(0)
        }

        if (isSizeChanged(w, h)) {
            val contentsScale = layer.contentScale.toDouble()
            val osView = layer.view!!
            val (w, h) = osView.frame.useContents {
                size.width to size.height
            }
            metalLayer.frame = osView.frame
            metalLayer.init(layer, device)
            metalLayer.drawableSize = CGSizeMake(w * metalLayer.contentsScale, h * metalLayer.contentsScale)
        }

        currentDrawable = metalLayer.nextDrawable()!!
        renderTarget = BackendRenderTarget.makeMetal(w, h, currentDrawable!!.texture.objcPtr())

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB
        )

        canvas = surface!!.canvas
    }

    fun flush() {
        // TODO: maybe make flush async as in JVM version.
        surface!!.flushAndSubmit()
        metalRedrawer.finishFrame()
    }

   fun rendererInfo(): String {
        return "Native Metal: device ${metalRedrawer.device.name}"
    }
}


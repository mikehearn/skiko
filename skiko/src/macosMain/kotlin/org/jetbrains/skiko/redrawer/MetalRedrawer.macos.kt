package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.*
import platform.CoreGraphics.CGColorCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextRef
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.QuartzCore.CAMetalDrawableProtocol
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCAGravityTopLeft
import platform.QuartzCore.kCALayerHeightSizable
import platform.QuartzCore.kCALayerWidthSizable
import kotlin.system.getTimeNanos
import platform.CoreGraphics.CGSizeMake

internal class MacOsMetalRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private var isDisposed = false
    internal val device = MTLCreateSystemDefaultDevice()!!
    private val queue = device.newCommandQueue()!!
    private var currentDrawable: CAMetalDrawableProtocol? = null
    private val metalLayer = MetalLayer()

    init {
        metalLayer.init(this.layer, device)
    }

    private val frameDispatcher = FrameDispatcher(SkikoDispatchers.Main) {
        if (layer.isShowing()) {
            layer.update(getTimeNanos())
            draw()
        }
    }

    fun makeContext() = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())

    fun makeRenderTarget(width: Int, height: Int): BackendRenderTarget {
        currentDrawable = metalLayer.nextDrawable()!!
        return BackendRenderTarget.makeMetal(width, height, currentDrawable!!.texture.objcPtr())
    }

    override fun dispose() {
        if (!isDisposed) {
            metalLayer.dispose()
            isDisposed = true
        }
    }

    override fun syncSize() {
        syncContentScale()
        val osView = layer.nsView!!
        val (w, h) = osView.frame.useContents {
            size.width to size.height
        }
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.frame = osView.frame
        metalLayer.init(layer, device)
        metalLayer.drawableSize = CGSizeMake(w * metalLayer.contentsScale, h * metalLayer.contentsScale)
        CATransaction.commit()
        CATransaction.flush()
    }

    private fun syncContentScale() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        metalLayer.contentsScale = layer.nsView.window!!.backingScaleFactor
        CATransaction.commit()
        CATransaction.flush()
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        layer.update(getTimeNanos())
        draw()
    }

    private fun draw() {
        // TODO: maybe make flush async as in JVM version.
        autoreleasepool {
            if (!isDisposed) {
                layer.draw()
            }
        }
    }

    fun finishFrame() {
        autoreleasepool {
            currentDrawable?.let {
                val commandBuffer = queue.commandBuffer()!!
                commandBuffer.label = "Present"
                commandBuffer.presentDrawable(it)
                commandBuffer.commit()
                currentDrawable = null
            }
        }
    }
}

class MetalLayer : CAMetalLayer {
    private lateinit var skiaLayer: SkiaLayer

    @OverrideInit
    constructor(): super()
    @OverrideInit
    constructor(layer: Any): super(layer)

    fun init(skiaLayer: SkiaLayer, theDevice: MTLDeviceProtocol) {
        this.setNeedsDisplayOnBoundsChange(true)
        this.removeAllAnimations()
        this.setAutoresizingMask(kCALayerWidthSizable or kCALayerHeightSizable )
        this.device = theDevice as objcnames.protocols.MTLDeviceProtocol?
        this.pixelFormat = MTLPixelFormatBGRA8Unorm
        this.opaque = false
        doubleArrayOf(0.0, 0.0, 0.0, 0.0).usePinned {
            this.backgroundColor =
                CGColorCreate(CGColorSpaceCreateDeviceRGB(), it.addressOf(0))
        }
        this.displaySyncEnabled = false
        skiaLayer.nsView.layer = this
        skiaLayer.nsView.wantsLayer = true
        this.contentsGravity = kCAGravityTopLeft;
    }

    fun dispose() {
        this.removeFromSuperlayer()
    }

    override fun drawInContext(ctx: CGContextRef?) {
        skiaLayer.update(getTimeNanos())
        skiaLayer.draw()
        super.drawInContext(ctx)
    }
}

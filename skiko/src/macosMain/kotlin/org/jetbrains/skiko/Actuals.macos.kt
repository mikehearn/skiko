package org.jetbrains.skiko

import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MacOsOpenGLContextHandler
import org.jetbrains.skiko.context.MacOsMetalContextHandler
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.MacOsMetalRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal actual fun createNativeContextHandler(
    layer: SkiaLayer, renderApi: GraphicsApi
): ContextHandler = when (renderApi) {
    GraphicsApi.OPENGL -> MacOsOpenGLContextHandler(layer)
    GraphicsApi.METAL -> MacOsMetalContextHandler(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}

internal actual fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> MacOsOpenGLRedrawer(layer, properties)
    GraphicsApi.METAL -> MacOsMetalRedrawer(layer, properties)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
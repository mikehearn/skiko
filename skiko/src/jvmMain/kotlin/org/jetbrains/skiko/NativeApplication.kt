package org.jetbrains.skiko

internal external fun getApplicationWindowCount(): Int

// don't forget to wrap all macOs native calls into this. otherwise there can be unpredictable memory leaks
internal fun <T> autoreleasepool(body: () -> T) : T {
    val handle = autoreleasePoolPush()
    return try {
        body()
    } finally {
        autoreleasePoolPop(handle)
    }
}

internal external fun autoreleasePoolPush(): Long
internal external fun autoreleasePoolPop(handle: Long)
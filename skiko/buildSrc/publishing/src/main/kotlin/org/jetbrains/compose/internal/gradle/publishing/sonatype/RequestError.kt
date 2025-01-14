package org.jetbrains.compose.internal.gradle.publishing.sonatype

import okhttp3.Request
import okhttp3.Response

internal class RequestError(
    val request: Request,
    val response: Response,
    responseBody: String
) : RuntimeException("${request.url}: returned ${response.code}\n${responseBody.trim()}")

internal fun RequestError(request: Request, response: Response): RequestError {
    var responseBodyException: Throwable? = null
    val responseBody = try {
        response.body?.string() ?: ""
    } catch (t: Throwable) {
        responseBodyException = t
        ""
    }
    return RequestError(request, response, responseBody).apply {
        if (responseBodyException != null) addSuppressed(responseBodyException)
    }
}
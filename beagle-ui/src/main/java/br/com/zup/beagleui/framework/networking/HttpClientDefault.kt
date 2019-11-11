package br.com.zup.beagleui.framework.networking

import br.com.zup.beagleui.framework.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

typealias OnSuccess = (responseData: ResponseData) -> Unit
typealias OnError = (throwable: Throwable) -> Unit

internal class HttpClientDefault(
    private val urlFactory: URLFactory = URLFactory()
) : HttpClient, CoroutineScope {

    private val job = Job()
    override val coroutineContext = job + CoroutineDispatchers.IO

    override fun execute(
        request: RequestData,
        onSuccess: OnSuccess,
        onError: OnError
    ): RequestCall {
        require(!getOrDeleteOrHeadHasData(request)) { "${request.method} does not support request body" }

        launch {
            doHttpRequest(request, onSuccess, onError)
        }

        return object: RequestCall {
            override fun cancel() {
                this@HttpClientDefault.cancel()
            }
        }
    }

    private fun getOrDeleteOrHeadHasData(request: RequestData): Boolean {
        return (request.method == HttpMethod.GET ||
                request.method == HttpMethod.DELETE ||
                request.method == HttpMethod.HEAD) &&
                request.body != null
    }

    private fun doHttpRequest(
        request: RequestData,
        onSuccess: OnSuccess,
        onError: OnError
    ) {
        val urlConnection = urlFactory.make(request.url).openConnection() as HttpURLConnection

        request.headers.forEach {
            urlConnection.setRequestProperty(it.key, it.value)
        }

        addRequestMethod(urlConnection, request.method)

        if (request.body != null) {
            setRequestBody(urlConnection, request.body)
        }

        try {
            onSuccess(createResponseData(urlConnection))
        } catch (e: Throwable) {
            onError(e)
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun addRequestMethod(urlConnection: HttpURLConnection, method: HttpMethod) {
        val methodValue = method.toString()

        if (method == HttpMethod.PATCH || method ==  HttpMethod.HEAD) {
            urlConnection.setRequestProperty("X-HTTP-Method-Override", methodValue)
            urlConnection.requestMethod = "POST"
        } else {
            urlConnection.requestMethod = methodValue
        }
    }

    private fun setRequestBody(urlConnection: HttpURLConnection, data: String) {
        urlConnection.outputStream.write(data.toByteArray())
        urlConnection.setRequestProperty("Content-Length", data.length.toString())
    }

    private fun createResponseData(urlConnection: HttpURLConnection): ResponseData {
        val byteArray = urlConnection.inputStream.readBytes()

        return ResponseData(
            statusCode = urlConnection.responseCode,
            headers = urlConnection.headerFields.map {
                Pair(it.key, it.value.toString())
            }.toMap(),
            data = byteArray
        )
    }
}
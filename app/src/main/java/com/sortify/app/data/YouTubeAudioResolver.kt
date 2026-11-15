package com.sortify.app.data

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.util.zip.GZIPInputStream

/** NewPipe handles player JavaScript signatures; only public, progressive audio is selected. */
object YouTubeAudioResolver {
    private val initialized by lazy {
        NewPipe.init(object : Downloader() {
            override fun execute(request: Request): Response {
                val connection = URI(request.url()).toURL().openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000; connection.readTimeout = 20_000
                connection.requestMethod = request.httpMethod()
                connection.setRequestProperty("User-Agent", YouTubeMusicClient.USER_AGENT)
                request.headers().forEach { (name, values) -> connection.setRequestProperty(name, values.joinToString(", ")) }
                try {
                    request.dataToSend()?.let { data ->
                        connection.doOutput = true
                        connection.outputStream.use { it.write(data) }
                    }
                    val code = connection.responseCode
                    val input = if (code in 200..299) connection.inputStream else connection.errorStream
                    val body = input?.let { raw ->
                        val stream = if (connection.contentEncoding.equals("gzip", true)) GZIPInputStream(raw) else raw
                        stream.bufferedReader().use { it.readText() }
                    }.orEmpty()
                    return Response(code, connection.responseMessage, connection.headerFields.filterKeys { it != null }, body, connection.url.toString())
                } finally { connection.disconnect() }
            }
        })
        true
    }

    fun resolve(videoId: String, quality: String): String {
        check(initialized)
        try {
            val extractor = ServiceList.YouTube.getStreamExtractor("https://www.youtube.com/watch?v=$videoId")
            extractor.fetchPage()
            return extractor.audioStreams.filter {
                it.isUrl && it.content.startsWith("https://") && it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP
            }.minByOrNull { kotlin.math.abs(it.averageBitrate - bitrate(quality)) }?.content
                ?: throw IOException("YouTube Music did not provide an audio stream for this track.")
        } catch (e: IOException) { throw e } catch (e: Exception) {
            throw IOException("YouTube Music could not resolve this track. Try another song or retry later.", e)
        }
    }
}

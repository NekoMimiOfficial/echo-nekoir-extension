package dev.brahmkshatriya.echo.extension.Nekoir

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.RequestBody.Companion.toRequestBody
import dev.brahmkshatriya.echo.extension.DataStore.getBaseApi
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.Feed.Companion.toFeed
import dev.brahmkshatriya.echo.common.models.NetworkRequest.Companion.toGetRequest
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toMedia
import dev.brahmkshatriya.echo.common.models.Streamable
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import kotlin.text.startsWith
import kotlin.collections.emptyList
import java.io.IOException
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.Lyrics
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.deserializeJsonStringToJsonObject
import dev.brahmkshatriya.echo.extension.safeGet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

const val TICKS_PER_MS = 10_000

class ApiService (settings: Settings) {
  private val BASE_API = getBaseApi(settings)

  private val SEARCH_ENDPOINT: String = BASE_API + "search"
  private val TRACK_ENDPOINT: String = BASE_API + "track/playback"
  private val ALBUM_ENDPOINT: String = BASE_API + "album/tracks"
  private val META_ENDPOINT: String = BASE_API + "track/metadata"

  private val HEADERS = Headers.headersOf("User-Agent", "ktor-client", "X-App-Version", "1.8")

  val client = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .build()

  internal suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
      continuation.invokeOnCancellation {
        cancel()
      }
      enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
          if (continuation.isCancelled) return
          continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
          continuation.resume(response)
        }
      })
    }
  }

  suspend fun getResp(
    client: OkHttpClient,
    url: String,
    params: JsonObject ?= null
  ): Response {
    val httpUrlBuilder = url.toHttpUrlOrNull()?.newBuilder()
      ?: throw IllegalArgumentException("Invalid URL: $url")

    params?.entries?.forEach {
      httpUrlBuilder.addQueryParameter(it.key, it.value.jsonPrimitive.content)
    }
    val finalUrl = httpUrlBuilder.build()

    val request = Request.Builder()
      .headers(HEADERS)
      .url(finalUrl)
      .get()
      .build()

    return client.newCall(request).await()
  }

  suspend fun search(query: String, qtype: String = "tracks"): String {
    val getParam = buildJsonObject{put("query", query); put("type", qtype)}
    val res = getResp(client, SEARCH_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  suspend fun track(track_id: String, track_quality: String): String {
    val getParam = buildJsonObject { put("id", track_id); put("quality", track_quality) }
    val res = getResp(client, TRACK_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  suspend fun album(album_id: String): String {
    val getParam = buildJsonObject { put("id", album_id) }
    val res = getResp(client, ALBUM_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  suspend fun metadata(track_id: String): String {
    val getParam = buildJsonObject { put("id", track_id) }
    val res = getResp(client, META_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  suspend fun getLyrics(clientId: String, track: Track): Feed<Lyrics> {
    val track_id = track.id
    var lyrics = "Loading..."
    var getReq = metadata(track_id)
    var jabba = deserializeJsonStringToJsonObject(getReq)
    while (jabba == null && getReq.contains("{\"detail\"")) {
      getReq = metadata(track_id)
      jabba = deserializeJsonStringToJsonObject(getReq)
    }
    if (jabba != null && jabba.containsKey("LYRICS")) {
      lyrics = safeGet("LYRICS", jabba, "Failed to load!")
    }
    val list: List<Lyrics.Item> = emptyList()
    val retList = PagedData.Single {
      listOf(
        Lyrics(
          id = "lyrics",
          title = "Lyrics",
          lyrics = Lyrics.Timed(list)
        )
      )
    }
    return retList.toFeed()
  }

  suspend fun getTrack(track: Track): Track {
    return track
  }

  suspend fun hires_handler(streamable: Streamable, qt: String): Streamable.Media {
    var getRequest = track(streamable.id, qt)
    while (getRequest.contains("detail") || getRequest.contains("[]")) {
      getRequest = track(streamable.id, qt)
    }
    val joe: JsonObject = deserializeJsonStringToJsonObject(getRequest)!!
    val manifest = safeGet("manifest", joe)
      .replace("\n", "")
      .replace("\r", "")
      .replace("\t", "")

    val resource = "http://nekomimi.tilde.team/API/v1/echo.php?data=$manifest"

    return Streamable.Source.Http(
      request = resource.toGetRequest(),
      type = Streamable.SourceType.DASH,
      quality = streamable.quality
    ).toMedia()
  }

  suspend fun getStreamableMedia(streamable: Streamable): Streamable.Media {
    var url: String = ""
    var qt: String = "LOW"
    if (streamable.quality == 96000) {qt = "HIGH"}
    if (streamable.quality == 114100) {qt = "LOSSLESS"}
    // For now we will revert this as wtf does the API even spit?
    if (streamable.quality == 192000) {qt = "HI_RES_LOSSLESS"; return hires_handler(streamable, qt)}
    print("${streamable.quality} $qt ")
    var getRequest = track(streamable.id, qt)
    while (getRequest.contains("detail") || getRequest.contains("[]")) {
      getRequest = track(streamable.id, qt)
    }
    val trackJson: JsonObject ?= deserializeJsonStringToJsonObject(getRequest)

    if (trackJson != null) {
      val jsonArrayElement = trackJson["urls"]?.jsonArray
      if (jsonArrayElement != null && jsonArrayElement.isNotEmpty()) {
        url = jsonArrayElement[0].jsonPrimitive.content
        println(url)
      }
    }

    return Streamable.Source.Http(
      request= url.toGetRequest(),
      type = Streamable.SourceType.Progressive,
      quality = streamable.quality,
    ).toMedia()
  }
}

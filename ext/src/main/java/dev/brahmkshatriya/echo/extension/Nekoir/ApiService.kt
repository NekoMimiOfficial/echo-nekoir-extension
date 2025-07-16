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
import dev.brahmkshatriya.echo.common.models.Request.Companion.toRequest
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toMedia
import dev.brahmkshatriya.echo.common.models.Streamable
import okhttp3.Headers
import okhttp3.HttpUrl
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

  fun getResp(
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

    return client.newCall(request).execute()
  }

  fun search(query: String, qtype: String = "tracks"): String {
    val getParam = buildJsonObject{put("query", query); put("type", qtype)}
    val res = getResp(client, SEARCH_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  fun track(track_id: String, track_quality: String): String {
    val getParam = buildJsonObject { put("id", track_id); put("quality", track_quality) }
    val res = getResp(client, TRACK_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  fun album(album_id: String): String {
    val getParam = buildJsonObject { put("id", album_id) }
    val res = getResp(client, ALBUM_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  fun metadata(track_id: String): String {
    val getParam = buildJsonObject { put("id", track_id) }
    val res = getResp(client, META_ENDPOINT, getParam)
    return res.body?.string() ?: "Some error occured: ${res.code}"
  }

  fun getLyrics(track: Track): PagedData<Lyrics> {
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
    return retList
  }

  suspend fun getTrack(track: Track): Track {
    val bigCoverImgUrl = track.extras["image"] ?: "/"
    val thumb = ImageHolder.UriImageHolder(uri = bigCoverImgUrl, crop = false)
    val final_track = Track (
      id = track.id,
      title = track.title,
      artists = track.artists,
      duration = track.duration,
      streamables = track.streamables,
      cover = thumb
    )
    return final_track
  }

  suspend fun getStreamableMedia(streamable: Streamable): Streamable.Media {
    var url: String = ""
    var qt: String = "LOSSLESS"
    // For now we will revert this as wtf does the API even spit?
    // if (streamable.quality > 44100)
    // {qt= "HI_RES_LOSSLESS"}
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
      request= url.toRequest(),
      type = Streamable.SourceType.Progressive,
      quality = streamable.quality,
    ).toMedia()
  }
}

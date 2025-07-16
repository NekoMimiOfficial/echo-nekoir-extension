package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.Request.Companion.toRequest
import dev.brahmkshatriya.echo.extension.Nekoir.constructTrackItem
import dev.brahmkshatriya.echo.extension.Nekoir.ApiService
import dev.brahmkshatriya.echo.extension.Screens.searchTrack
import dev.brahmkshatriya.echo.extension.randomString
import dev.brahmkshatriya.echo.common.settings.Settings
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlin.collections.mutableListOf
import kotlin.collections.emptyList
import kotlin.collections.listOf

class UiBuilder {
  fun itemGetter(term: String, settings: Settings, force: Boolean = false): List<EchoMediaItem> {
    val api = ApiService(settings)
    var searchReq = api.search(term)
    var items: MutableList<EchoMediaItem> = mutableListOf()

    while (searchReq.contains("{\"detail\"") || force && searchReq.contains("[]")) {
      searchReq= api.search(term)
    }

    val reqJson: List<JsonObject>? = deserializeJsonStringToListOfJsonObjects(searchReq)

    reqJson?.let { list ->
      list.forEach { jsonObject ->
        var format: MutableList<Int> = mutableListOf()

        val title = safeGet("title", jsonObject)
        val cover = safeGet("cover", jsonObject, "http://nekomimi.tilde.team/pool/05/missingno")
        val id = safeGet("id", jsonObject)
        val duration = safeGet("duration", jsonObject, "0:39")

        if (jsonObject.containsKey("formats")) {
          val arrayItemsFormats = jsonObject["formats"]?.jsonArray?.map {it.jsonPrimitive.content}
          if (arrayItemsFormats != null) {
            for (forma in arrayItemsFormats) {
              if (forma.startsWith("HIRES")) { format.add(96000) } else { format.add(44100) }
            }
          }
        }

        val time_m = duration.split(":")[0].toInt()
        val time_s = duration.split(":")[1].toInt()
        items.add(
          constructTrackItem(
            title,
            id,
            cover,
            format,
            time_m,
            time_s
          )
        )
      }
    }

    return items
  }

  fun getSearchFeedFor(term: String, settings: Settings, ident: String = "Search results for:", force: Boolean = false): Shelf {
    val items = itemGetter(term, settings, force)
    return Shelf.Lists.Items(
      title = "$ident $term",
      list = items
    )
  }

  fun getSearchHor(term: String, settings: Settings, force: Boolean = false): List<Shelf> {
    var horter: MutableList<Shelf> = mutableListOf()
    val items = itemGetter(term, settings, force)
    for (item in items) {
      horter.add(Shelf.Item(media= item))
    }
    return horter
  }


  fun getRandomShelves(settings: Settings): List<Shelf> {
    val eastern: List<String> = listOf(
      "YOASOBI",
      "ZUTOMAYO",
      "Deco*27",
      "Deco*27",
      "Deco*27",
      "Deco*27",
      "Ui Shigure",
      "kenshi",
      "ado",
      "midnight grand orchestra",
      "PinocchoiP",
      "rainych",
      "fuji kaze",
      "Mrs. Green Apple",
      "Mafumafu",
      "THE ORAL CIGARETTES",
    )

    val mosika: List<String> = listOf(
      "Polyphia",
      "Joe Satriani",
      "Ratatat",
      "Stars of the lid",
      "Snail's House",
      "Animals as Leaders"
    )

    val western: List<String> = listOf(
      "Maroon 5",
      "Sia",
      "AC/DC",
      "Daft Punk",
      "Eddie Johns",
      "Red Hot Chili Peppers",
      "Michael Jackson",
      "Owl City",
      "Imagine Dragons"
    )

    val foreign: List<String> = listOf(
      "Gipsy Kings"
    )

    val niche: List<String> = listOf(
      "porter robinson",
      "heiakim",
      "OMFG",
      "Moe Shop",
      "Dion Timmer"
    )

    val s1 = eastern.random()
    val s2 = western.random()
    val s3 = mosika.random()
    val s4 = foreign.random()
    val s5 = niche.random()

    val sh1 = getSearchFeedFor(s1, settings, "Random Eastern Pick:", true)
    val sh2 = getSearchFeedFor(s2, settings, "Random Western Pick:", true)
    val sh3 = getSearchFeedFor(s3, settings, "Random Instrumental:", true)
    val sh4 = getSearchFeedFor(s4, settings, "Random Cultural Pick:", true)
    val sh5 = getSearchFeedFor(s5, settings, "Random Half Vocal Pick:", true)

    val randFeed: List<Shelf> = listOf(sh1, sh2, sh3, sh4, sh5)
    return randFeed
  }
}

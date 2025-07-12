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

class UiBuilder
{
  fun getSearchFeedFor(term: String, settings: Settings, ident: String = "Search results for:", force: Boolean = false): Shelf
  {
    val api= ApiService(settings)
    var searchReq= api.search(term)
    var titles: MutableList<String> = mutableListOf()
    var covers: MutableList<String> = mutableListOf()
    var items: MutableList<EchoMediaItem> = mutableListOf()
    var ids: MutableList<String> = mutableListOf()
    var formats: MutableList<MutableList<Int>> = mutableListOf()

    while (searchReq.contains("{\"detail\"") || force && searchReq.contains("[]"))
    {searchReq= api.search(term)}

    val reqJson: List<JsonObject>? = deserializeJsonStringToListOfJsonObjects(searchReq)

    reqJson?.let { list ->
      list.forEachIndexed { _, jsonObject ->
        if (jsonObject.containsKey("title")) {
          val key2Value = jsonObject["title"]?.jsonPrimitive?.content
          val name= key2Value ?: "error"
          titles.add(name)
        }

        if (jsonObject.containsKey("cover")) {
          val covUrl= jsonObject["cover"]?.jsonPrimitive?.content ?: "http://nekomimi.tilde.team/pool/05/missingno.png"
          covers.add(covUrl)
        }

        if (jsonObject.containsKey("id")) {
          val id= jsonObject["id"]?.jsonPrimitive?.content ?: randomString(8)
          ids.add(id)
        }

        if (jsonObject.containsKey("formats")) {
          val arrayItemsFormats= jsonObject["formats"]?.jsonArray?.map {it.jsonPrimitive.content}
          var fmt: MutableList<Int> = mutableListOf()
          if (arrayItemsFormats != null)
          {
            for (forma in arrayItemsFormats)
            {if (forma.startsWith("HIRES")) {fmt.add(96000)}else{fmt.add(44100)}}
          }
          formats.add(fmt)
        }
      }
    }

    for (i in 0..titles.size-1)
    {
      val item= constructTrackItem(
        titles[i],
        ids[i],
        covers[i],
        formats[i],
      )

      items.add(item)
    }
    return Shelf.Lists.Items(
      title= "$ident $term",
      list= items
    )
  }

  fun getRandomShelves(settings: Settings): List<Shelf>
  {
    val eastern: List<String> = listOf(
      "YOASOBI",
      "ZUTOMAYO",
      "Deco*27",
      "Ui Shigure",
      "kenshi",
      "ado",
      "midnight grand orchestra",
      "PinocchoiP",
      "rainych",
      "fuji kaze",
      "Mrs. Green Apple",
      "THE ORAL CIGARETTES"
    )

    val mosika: List<String> = listOf(
      "Polyphia",
      "porter robinson"
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

    val s1= eastern.random()
    val s2= western.random()
    val s3= mosika.random()
    val s4= foreign.random()

    val sh1= getSearchFeedFor(s1, settings, "Random Eastern Pick:", true)
    val sh2= getSearchFeedFor(s2, settings, "Random Western Pick:", true)
    val sh3= getSearchFeedFor(s3, settings, "Random Instrumental:", true)
    val sh4= getSearchFeedFor(s4, settings, "Random Cultural Pick:", true)

    val randFeed: List<Shelf> = listOf(sh1, sh2, sh3, sh4)
    return randFeed
  }
}

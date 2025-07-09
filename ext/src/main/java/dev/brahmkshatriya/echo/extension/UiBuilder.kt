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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlin.collections.mutableListOf
import kotlin.collections.emptyList

class UiBuilder
{
  fun getPPShelf
  (
    shelfTitle: String,
  ): Shelf
  {
    val media_ZTMY_kirakiller= constructTrackItem(
      "Kira Killer",
      "297355391",
      emptyList(),
      4,
      13,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/6857c0fe/be21/4cfb/9651/9487cedb229b/80x80.jpg",
      "https://resources.tidal.com/images/6857c0fe/be21/4cfb/9651/9487cedb229b/1280x1280.jpg"
    )

    val media_ZTMY_mirrortune= constructTrackItem(
      "Mirror Tune",
      "223245222",
      emptyList(),
      4,
      9,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/1a8eeb5a/efda/4254/865f/fc5c4b69548c/80x80.jpg",
      "https://resources.tidal.com/images/1a8eeb5a/efda/4254/865f/fc5c4b69548c/1280x1280.jpg"
    )

    val media_ZTMY_nekoreset= constructTrackItem(
      "Neko Reset",
      "206613287",
      emptyList(),
      4, 7,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/eaa6215b/a2da/4f58/92f4/a7564bb4e526/80x80.jpg",
      "https://resources.tidal.com/images/eaa6215b/a2da/4f58/92f4/a7564bb4e526/1280x1280.jpg"
    )

    val media_YOASOBI_undead= constructTrackItem(
      "Undead",
      "370244000",
      emptyList(),
      3, 3,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/b4ca1006/eadf/412b/b7a5/d55782c634ef/80x80.jpg",
      "https://resources.tidal.com/images/b4ca1006/eadf/412b/b7a5/d55782c634ef/1280x1280.jpg"
    )

    val media_YOASOBI_watchme= constructTrackItem(
      "Watch me!",
      "434940614",
      emptyList(),
      3, 6, 
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/cc45e83f/ed2d/4637/8895/13a1cf955134/80x80.jpg",
      "https://resources.tidal.com/images/cc45e83f/ed2d/4637/8895/13a1cf955134/1280x1280.jpg"
    )

    val items= listOf(
      media_ZTMY_kirakiller,
      media_ZTMY_mirrortune,
      media_ZTMY_nekoreset,
      media_YOASOBI_undead,
      media_YOASOBI_watchme
    )

    return Shelf.Lists.Items(
      title= shelfTitle,
      list= items,
    )
  }

  fun getSpecialFeed(): Shelf
  {
    val media_POLY_playinggod= constructTrackItem(
      "Playing God",
      "227095354",
      emptyList(),
      3, 26,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/bdbc409c/d7a5/494e/83be/a38abddfdb74/80x80.jpg",
      "https://resources.tidal.com/images/bdbc409c/d7a5/494e/83be/a38abddfdb74/1280x1280.jpg"
    )

    val media_POLY_egodeath= constructTrackItem(
      "Ego Death",
      "248513132",
      emptyList(),
      5, 50,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/705218dc/222b/4fd4/bc99/6c073238029b/80x80.jpg",
      "https://resources.tidal.com/images/705218dc/222b/4fd4/bc99/6c073238029b/1280x1280.jpg"
    )

    val media_POLY_40oz= constructTrackItem(
      "40oz",
      "76045944",
      emptyList(),
      3, 53,
      listOf(""),
      listOf(44100),
      "https://resources.tidal.com/images/6d253e97/42c4/44d9/9dba/83a01d9033f3/80x80.jpg",
      "https://resources.tidal.com/images/6d253e97/42c4/44d9/9dba/83a01d9033f3/1280x1280.jpg"
    )

    val items= listOf(
      media_POLY_playinggod,
      media_POLY_egodeath,
      media_POLY_40oz
    )

    return Shelf.Lists.Items(
      title= "Today's special: Polyphia",
      list= items
    )
  }

  fun getSearchFeedFor(term: String): Shelf
  {
    val api= ApiService()
    val searchReq= api.search(term)
    val reqJson: List<JsonObject>? = deserializeJsonStringToListOfJsonObjects(searchReq)
    var titles: MutableList<String> = mutableListOf()
    var covers: MutableList<String> = mutableListOf()
    var items: MutableList<EchoMediaItem> = mutableListOf()
    var ids: MutableList<String> = mutableListOf()

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
      }
    }

    for (i in 0..titles.size-1)
    {
      val item= constructTrackItem(
        titles[i],
        ids[i],
        emptyList(),
        4, 4,
        listOf(""),
        listOf(44100),
        covers[i],
        covers[i].replace("80x80.jpg", "1280x1280.jpg")
      )

      items.add(item)
    }
    return Shelf.Lists.Items(
      title= "Search results for: $term",
      list= items
    )
  }
}

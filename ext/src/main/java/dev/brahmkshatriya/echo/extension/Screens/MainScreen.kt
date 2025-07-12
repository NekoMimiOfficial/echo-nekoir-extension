package dev.brahmkshatriya.echo.extension.Screens

import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.models.Feed.Companion.toFeed
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.NekoirMain
import kotlinx.coroutines.Dispatchers
import dev.brahmkshatriya.echo.extension.UiBuilder

val ui= UiBuilder()

fun createHomeFeed(settings: Settings): Feed {
  // val pd= PagedData.Single{
    // listOf(ui.getPPShelf("Personal Picks"), ui.getSpecialFeed())
  // }
  val pd= PagedData.Single{ ui.getRandomShelves(settings) }
  return pd.toFeed()
}

fun searchTrack(query: String, settings: Settings): Feed
{
  val pd= PagedData.Single{listOf(ui.getSearchFeedFor(query, settings))}
  return pd.toFeed()
}

fun searchAlbum(query: String, settings: Settings): Feed
{
  val pd= PagedData.Single{listOf(ui.getSearchFeedFor(query, settings))}
  return pd.toFeed()
}

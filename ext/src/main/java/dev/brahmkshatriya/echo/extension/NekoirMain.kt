package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.HomeFeedClient
import dev.brahmkshatriya.echo.common.clients.LyricsClient
import dev.brahmkshatriya.echo.common.clients.PlaylistEditClient
import dev.brahmkshatriya.echo.common.clients.SearchFeedClient
import dev.brahmkshatriya.echo.common.clients.TrackClient
import dev.brahmkshatriya.echo.common.clients.TrackLikeClient
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Lyrics
import dev.brahmkshatriya.echo.common.models.QuickSearchItem
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.User
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.common.settings.SettingTextInput
import dev.brahmkshatriya.echo.extension.Screens.createHomeFeed
import dev.brahmkshatriya.echo.extension.Screens.searchTrack
import dev.brahmkshatriya.echo.extension.Screens.searchAlbum
import dev.brahmkshatriya.echo.extension.Nekoir.ApiService

const val SETTINGS_DEVICE_ID_KEY = "device_id"
const val SETTINGS_HISTORY_KEY = "search_history"

class NekoirMain :
  HomeFeedClient,
  ExtensionClient,
  SearchFeedClient,
  TrackClient{

  // Settings fraq
  override suspend fun onExtensionSelected() {}

  val setting_base_api= SettingTextInput(title= "Echoir API URL", key= "bapi64")

  override val settingItems: List<Setting> = listOf(setting_base_api)

  private lateinit var setting: Settings

  override fun setSettings(settings: Settings) {
    setting = settings
  }

  val deviceId: String
    get() = setting.getString(SETTINGS_DEVICE_ID_KEY).orEmpty().ifBlank {
      randomString().also { setting.putString(SETTINGS_DEVICE_ID_KEY, it) }
    }
  
  val ui by lazy { UiBuilder() }
  val api by lazy { ApiService(setting) }
  // ---------------------------------------------------

  // Home Feed Frag
  override suspend fun getHomeTabs(): List<Tab>
  {return emptyList()}

  override fun getHomeFeed(tab: Tab?): Feed
  {return createHomeFeed(setting)}
  // ---------------------------------------------------
  
  // Search Frag
  private var searchHistory: List<String>
    get() = setting.getString(SETTINGS_HISTORY_KEY)
      ?.split(",")?.distinct()?.filter(String::isNotBlank)?.take(5)
      ?: emptyList()
    set(value) = setting.putString(SETTINGS_HISTORY_KEY,
    value.joinToString(","))

  private fun saveQueryToHistory(query: String)
  {
    val history = searchHistory.toMutableList()
    history.add(0, query)
    searchHistory = history
  }

  override suspend fun quickSearch(query: String): List<QuickSearchItem>
  {
    return if (query.isBlank()) {
      searchHistory.map { QuickSearchItem.Query(it, true) }
    }else{
      emptyList()
    }
  }

  override suspend fun deleteQuickSearch(item: QuickSearchItem)
  {
    searchHistory -= item.title
  }

  override suspend fun searchTabs(query: String): List<Tab>
  {return listOf(Tab("tracks", "Tracks"), Tab("albums", "Albums"))}

  override fun searchFeed
  (
    query: String,
    tab: Tab?,
  ): Feed
  {
    saveQueryToHistory(query)

    return when (tab?.id) {
      "tracks" -> searchTrack(query, setting)
      "albums" -> searchAlbum(query, setting)
      else -> throw IllegalArgumentException("Invalid search tab")
    }
  }
  // ---------------------------------------------------

  // Track Frag
  override suspend fun loadTrack(track: Track): Track
  {
    return api.getTrack(track)
  }

  override suspend fun loadStreamableMedia
  (
    streamable: Streamable,
    isDownload: Boolean,
  ): Streamable.Media 
  {
    return api.getStreamableMedia(streamable)
  }

  override fun getShelves(track: Track): PagedData<Shelf>
  {
    return PagedData.Single { emptyList() }
  }
  // --------------------------------------------------
}

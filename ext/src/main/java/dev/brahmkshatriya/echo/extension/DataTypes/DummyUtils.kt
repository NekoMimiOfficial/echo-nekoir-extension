package dev.brahmkshatriya.echo.extension.DataTypes

import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.Album
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.Shelf
import dev.brahmkshatriya.echo.common.models.Feed
import dev.brahmkshatriya.echo.common.helpers.PagedData

fun createDummyTrack(id: String = "dummy_track_id", title: String = "Dummy Track Title"): Track {
    return Track(
        id = id,
        title = title,
        artists = listOf(Artist(id = "dummy_artist_id", name = "Dummy Artist")),
        album = Album(id = "dummy_album_id", title = "Dummy Album"),
        duration = 180000L, // 3 minutes in milliseconds
        isLiked = false,
    )
}

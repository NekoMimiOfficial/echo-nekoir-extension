package dev.brahmkshatriya.echo.extension.Nekoir

import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.Album

fun constructTrackItem(
  title: String,
  id: String,
  cover: String = "http://nekomimi.tilde.team/pool/05/missingno.png",
  qualities: List<Int> = listOf(44100),
  d_min: Int = 0,
  d_sec: Int = 39,
  artists: List<Artist> = emptyList(),
): EchoMediaItem {
  val thumb= ImageHolder.UriImageHolder(uri= cover, crop= false)
  val duration = 1000 * ( 60 * d_min + d_sec )
  val streams: MutableList<Streamable> = mutableListOf()

  for (quality in qualities) {
    var internal_quality: String = "Lossless"
    if (quality > 44100) {internal_quality= "HiRes Lossless"}
    val streamable= Streamable(id= id, title= internal_quality,
      type= Streamable.MediaType.Server, quality= quality)

    streams.add(streamable)
  }

  val track = Track(
    id = id,
    title = title,
    artists = artists,
    duration = duration.toLong(),
    cover = thumb,
    streamables = streams,
    extras = mapOf( Pair("image", cover.replace("80x80.", "1280x1280.")) )
  )

  return track.toMediaItem()
}

fun constructAlbumItem(
  title: String,
  id: String,
  cover: String = "http://nekomimi.tilde.team/pool/05/missingno.png"
): EchoMediaItem.Lists.AlbumItem {
  val thumb = ImageHolder.UriImageHolder(uri= cover, crop= false)

  return Album(
    title = title,
    id = id,
    cover = thumb
  ).toMediaItem()
}

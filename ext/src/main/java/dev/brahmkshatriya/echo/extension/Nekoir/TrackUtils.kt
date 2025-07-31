package dev.brahmkshatriya.echo.extension.Nekoir

import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
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
  val thumb= cover.replace("80x80.", "640x640.").toImageHolder()
  val duration = 1000 * ( 60 * d_min + d_sec )
  val streams: MutableList<Streamable> = mutableListOf()

  for (quality in qualities) {
    var internal_quality: String = "Other Qualtiy"
    if (quality == 192000) {internal_quality= "Master (HiRes Lossless)"}
    if (quality == 114100) {internal_quality= "HiFi (Lossless)"}
    if (quality == 96000) {internal_quality= "High (320kbps)"}
    if (quality == 32000) {internal_quality= "Low (96kbps)"}
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
  )

  return track.toMediaItem()
}

fun constructAlbumItem(
  title: String,
  id: String,
  cover: String = "http://nekomimi.tilde.team/pool/05/missingno.png"
): EchoMediaItem.Lists.AlbumItem {
  val thumb = cover.toImageHolder()

  return Album(
    title = title,
    id = id,
    cover = thumb
  ).toMediaItem()
}

package dev.brahmkshatriya.echo.extension.Nekoir

import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.EchoMediaItem
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.ImageHolder

fun constructTrackItem
(
  title: String,
  id: String,
  artists: List<Artist> = emptyList(),
  d_min: Int = 0,
  d_sec: Int = 39,
  urls: List<String> = listOf(""),
  qualities: List<Int> = listOf(44100),
  cover: String = "http://nekomimi.tilde.team/pool/05/missingno.png",
  big_cover: String = "http://nekomimi.tilde.team/pool/05/missingno.png"
): EchoMediaItem
{
  val thumb= ImageHolder.UriImageHolder(uri= cover, crop= false)
  val duration = 1000 * ( 60 * d_min + d_sec )
  val streams: MutableList<Streamable> = mutableListOf()

  var internal_quality: String = "Lossless"
  if (qualities[0] > 44100) {internal_quality= "HiRes Lossless"}
  val s1= Streamable(
    id= id,
    quality= qualities[0],
    type= Streamable.MediaType.Server,
    title= internal_quality,
    extras= mapOf( Pair("url", urls[0]) )
  )

  streams.add(s1)
  val track= Track(
    id= id,
    title= title,
    artists= artists,
    duration= duration.toLong(),
    cover= thumb,
    streamables= streams,
    extras= mapOf( Pair("image", big_cover) )
  )

  return track.toMediaItem()
}

package dev.brahmkshatriya.echo.extension.DataTypes

import kotlinx.serialization.Serializable

@Serializable
data class Track
(
  val track_id: Int,
  val album_id: Int,

  val title_name: String,
  val album_name: String,
  val artist_name: String
)

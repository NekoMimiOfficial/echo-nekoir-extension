package dev.brahmkshatriya.echo.extension.DataStore

import dev.brahmkshatriya.echo.common.settings.Settings

fun getBaseApi(): String
{
  lateinit var store: Settings
  var api= store.getString("bapi64")
  api= api ?: "/"
  if (api.endsWith("/"))
  {}else{api= api+"/"; store.putString("bapi64", api)}
  return api
}

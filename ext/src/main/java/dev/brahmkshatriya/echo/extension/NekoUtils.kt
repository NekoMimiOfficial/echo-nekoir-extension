package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.models.ImageHolder
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

fun placeFn ()
{return}

fun randomString(length: Int = 16): String {
    val charPool = ('a'..'z') + ('0'..'9')

    return buildString(length) {
        for (i in 0 until length) {
            append(charPool.random())
        }
    }
}

fun String.getImageUrl(
    serverUrl: String,
    id: String,
    name: String = "Primary",
    index: Int? = null,
): ImageHolder {
    return serverUrl.toHttpUrl().newBuilder().apply {
        addPathSegment("Items")
        addPathSegment(id)
        addPathSegment("Images")
        addPathSegment(name)
        index?.let { addPathSegment(it.toString()) }
        addQueryParameter("tag", this@getImageUrl)
    }.build().toString().toImageHolder(crop = true)
}

fun deserializeJsonStringToJsonObject(jsonString: String): JsonObject? {
    return try {
        // First, parse the string into a generic JsonElement.
        val jsonElement = json.parseToJsonElement(jsonString)

        // Check if the parsed element is actually a JsonObject
        if (jsonElement is JsonObject) {
            jsonElement
        } else {
            println("JSON string is not a single JSON object: $jsonString")
            null
        }
    } catch (e: SerializationException) {
        println("Error deserializing JSON string to JsonObject: ${e.message}")
        null
    } catch (e: IllegalArgumentException) {
        println("Invalid JSON string or data for JsonObject: ${e.message}")
        null
    }
}

fun deserializeJsonStringToListOfJsonObjects(jsonString: String): List<JsonObject>? {
    return try {
        val jsonElement = json.parseToJsonElement(jsonString)
        if (jsonElement is JsonArray) {
            jsonElement.map { it.jsonObject }
        } else {
            println("JSON string is not a JSON array: $jsonString")
            null
        }
    } catch (e: SerializationException) {
        println("Error deserializing JSON string to List<JsonObject>: ${e.message}")
        null
    } catch (e: IllegalArgumentException) {
        println("Invalid JSON string or data for List<JsonObject>: ${e.message}")
        null
    }
}

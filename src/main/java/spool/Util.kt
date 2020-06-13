package spool

import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonPrimitive

fun String.json(): JsonPrimitive {
    return JsonPrimitive(this)
}

fun Boolean.json(): JsonPrimitive {
    return if (this) { JsonPrimitive.TRUE } else { JsonPrimitive.FALSE }
}

fun Iterable<JsonElement>.json(): JsonElement {
    val array = JsonArray()
    this.forEach { array.add(it) }
    return array
}
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

fun <T, U> pairedList(tList: List<T>, uList: List<U>): List<Pair<T, U>> {
    if (tList.size != uList.size) throw Exception()

    val pairList: MutableList<Pair<T, U>> = mutableListOf()

    for (indexed in tList.withIndex()) {
        pairList.add(Pair(indexed.value, uList[indexed.index]))
    }

    return pairList
}
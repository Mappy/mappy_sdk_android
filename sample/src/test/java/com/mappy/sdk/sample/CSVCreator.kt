package com.mappy.sdk.sample

import java.io.File
import java.io.FileReader

fun main() {
    val points = loadPoints()
    for (point in points) {
        println("${point.lng};${point.lat}")
    }
}

/**
 * geometry1.json is a part of a route returned by Mappy WS (journey Paris intra-muros) defined by "geometry" key
 */
fun loadPoints(): List<Point> {
    val points = mutableListOf<Point>()
    val jsonFile = File("src/test/resources/geometry1.json")
    val reader = FileReader(jsonFile)
    reader.readLines().forEach {
        it.split(",").toTypedArray().also {
            points.add(
                Point(
                    it[0].substring(1).trim().toDouble(),
                    it[1].substring(0, it[1].length - 1).trim().toDouble()
                )
            )
        }
    }
    return points
}


data class Point(val lat: Double, val lng: Double)

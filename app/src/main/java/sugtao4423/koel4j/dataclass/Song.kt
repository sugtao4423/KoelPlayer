package sugtao4423.koel4j.dataclass

import java.io.Serializable
import java.util.*

data class Song(
    val id: String,
    val album: Album,
    val artist: Artist,
    val title: String,
    val length: Double,
    val track: Int,
    val disc: Int,
    val createdAt: Date
) : Serializable
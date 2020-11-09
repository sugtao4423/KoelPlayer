package sugtao4423.koel4j.dataclass

import java.io.Serializable
import java.util.*

data class Album(
    val id: Int,
    val artist: Artist,
    val name: String,
    val cover: String,
    val createdAt: Date,
    val isCompilation: Boolean
) : Serializable
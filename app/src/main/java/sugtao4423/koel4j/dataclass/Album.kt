package sugtao4423.koel4j.dataclass

import java.util.*

data class Album(
    val id: Int,
    val artistId: Int,
    val name: String,
    val cover: String,
    val createdAt: Date,
    val isCompilation: Boolean
)
package sugtao4423.koel4j.dataclass

import java.io.Serializable

data class Artist(
    val id: Int,
    val name: String,
    val image: String?
) : Serializable
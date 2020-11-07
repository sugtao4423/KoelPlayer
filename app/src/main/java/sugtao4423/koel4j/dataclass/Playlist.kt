package sugtao4423.koel4j.dataclass

import java.io.Serializable

data class Playlist(
    val id: Int,
    val name: String,
    val songs: List<String>
) : Serializable
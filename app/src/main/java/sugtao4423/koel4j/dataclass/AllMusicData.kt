package sugtao4423.koel4j.dataclass

data class AllMusicData(
    val albums: List<Album>,
    val artists: List<Artist>,
    val songs: List<Song>,
    val playlists: List<Playlist>
)
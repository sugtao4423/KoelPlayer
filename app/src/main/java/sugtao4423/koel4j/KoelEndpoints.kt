package sugtao4423.koel4j

object KoelEndpoints {

    const val AUTHENTICATION = "/api/me"

    const val ARTISTS = "/api/artists"

    const val ALBUMS = "/api/albums"

    const val SONGS = "/api/songs"

    const val PLAYLISTS = "/api/playlists"

    fun playlistSongs(playlistId: String): String {
        return "/api/playlists/$playlistId/songs"
    }

    fun musicFile(auth: String, songId: String): String {
        return "/play/${songId}?t=$auth"
    }

}

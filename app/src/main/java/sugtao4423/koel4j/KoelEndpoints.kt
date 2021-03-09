package sugtao4423.koel4j

class KoelEndpoints {

    companion object {
        const val authentication = "/api/me"
        const val applicationData = "/api/data"

        fun playlistData(playlistId: Int): String {
            return "/api/playlist/$playlistId/songs"
        }

        fun musicFile(auth: String, songId: String): String {
            return "/play/${songId}?api_token=$auth"
        }
    }

}
package sugtao4423.koel4j

class KoelEndpoints {

    companion object {
        const val authentication = "/api/me"
        const val applicationData = "/api/data"

        fun playlistData(playlistId: Int): String {
            return "/api/playlist/$playlistId/songs"
        }
    }

}
package sugtao4423.koelplayer.musicdb

import android.content.Context
import sugtao4423.koel4j.dataclass.*

class MusicDB(private val context: Context) {

    private val db = MusicDBHelper(context).writableDatabase

    fun close() {
        db.close()
    }

    fun resetDatabase() {
        val tables = arrayOf("albums", "artists", "songs", "playlists")
        tables.map {
            db.execSQL("DROP TABLE $it")
        }
        MusicDBHelper(context).onCreate(db)
    }

    fun saveAllMusicData(musicData: AllMusicData) {
        insertAlbumData(musicData.albums)
        insertArtistData(musicData.artists)
        insertSongData(musicData.songs)
        insertPlaylist(musicData.playlists)
    }

    private fun insertAlbumData(albums: List<Album>) {
        val sql = "INSERT INTO albums VALUES (?, ?, ?, ?, ?, ?)"
        albums.map {
            val bindArgs = arrayOf(
                it.id.toString(),
                it.artistId.toString(),
                it.name,
                it.cover,
                it.createdAt.time.toString(),
                it.isCompilation.toString()
            )
            db.compileStatement(sql).apply {
                bindAllArgsAsStrings(bindArgs)
                execute()
                close()
            }
        }
    }

    private fun insertArtistData(artists: List<Artist>) {
        val sql = "INSERT INTO artists VALUES (?, ?, ?)"
        artists.map {
            val bindArgs = arrayOf(
                it.id.toString(), it.name, it.image
            )
            db.compileStatement(sql).apply {
                bindAllArgsAsStrings(bindArgs)
                execute()
                close()
            }
        }
    }

    private fun insertSongData(songs: List<Song>) {
        val sql = "INSERT INTO songs VALUES(?, ?, ?, ?, ?, ?, ?, ?)"
        songs.map {
            val bindArgs = arrayOf(
                it.id,
                it.albumId.toString(),
                it.artistId.toString(),
                it.title,
                it.length.toString(),
                it.track.toString(),
                it.disc.toString(),
                it.createdAt.time.toString()
            )
            db.compileStatement(sql).apply {
                bindAllArgsAsStrings(bindArgs)
                execute()
                close()
            }
        }
    }

    private fun insertPlaylist(playlists: List<Playlist>) {
        val sql = "INSERT INTO playlists VALUES (?, ?, ?)"
        playlists.map {
            val bindArgs = arrayOf(
                it.id.toString(),
                it.name,
                it.songs.joinToString(",")
            )
            db.compileStatement(sql).apply {
                bindAllArgsAsStrings(bindArgs)
                execute()
                close()
            }
        }
    }

}
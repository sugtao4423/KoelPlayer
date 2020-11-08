package sugtao4423.koelplayer.musicdb

import android.content.Context
import android.database.Cursor
import sugtao4423.koel4j.dataclass.*
import java.util.*
import kotlin.collections.ArrayList

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

    fun getAlbumSongs(albumId: Int): List<Song> {
        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery("SELECT * FROM songs WHERE albumId = ?", arrayOf(albumId.toString()))
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()
        songs.sortBy { it.track }
        return songs
    }

    fun getArtist(artistId: Int): Artist {
        val artistCursor = db.rawQuery("SELECT * FROM artists WHERE id = ?", arrayOf(artistId.toString()))
        artistCursor.moveToNext()
        val artist = getArtistData(artistCursor)
        artistCursor.close()
        return artist
    }

    fun getAllMusicData(): AllMusicData {
        val albums = ArrayList<Album>()
        val albumCursor = db.rawQuery("SELECT * FROM albums", null)
        while (albumCursor.moveToNext()) {
            albums.add(getAlbumData(albumCursor))
        }
        albumCursor.close()

        val artists = ArrayList<Artist>()
        val artistCursor = db.rawQuery("SELECT * FROM artists", null)
        while (artistCursor.moveToNext()) {
            artists.add(getArtistData(artistCursor))
        }
        artistCursor.close()

        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery("SELECT * FROM songs", null)
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()

        val playlists = ArrayList<Playlist>()
        val playlistCursor = db.rawQuery("SELECT * FROM playlists", null)
        while (playlistCursor.moveToNext()) {
            playlists.add(getPlaylistData(playlistCursor))
        }
        playlistCursor.close()

        return AllMusicData(albums, artists, songs, playlists)
    }

    private fun getAlbumData(c: Cursor): Album {
        c.apply {
            val id = c.getInt(0)
            val artistId = c.getInt(1)
            val name = c.getString(2)
            val cover = c.getString(3)
            val createdAt = Date(c.getLong(4) * 1000)
            val isCompilation = c.getString(5).toBoolean()

            return Album(id, artistId, name, cover, createdAt, isCompilation)
        }
    }

    private fun getArtistData(c: Cursor): Artist {
        c.apply {
            val id = c.getInt(0)
            val name = c.getString(1)
            val image = c.getString(2)

            return Artist(id, name, image)
        }
    }

    private fun getSongData(c: Cursor): Song {
        c.apply {
            val id = c.getString(0)
            val albumId = c.getInt(1)
            val artistId = c.getInt(2)
            val title = c.getString(3)
            val length = c.getDouble(4)
            val track = c.getInt(5)
            val disc = c.getInt(6)
            val createdAt = Date(c.getLong(7) * 1000)

            return Song(id, albumId, artistId, title, length, track, disc, createdAt)
        }
    }

    private fun getPlaylistData(c: Cursor): Playlist {
        c.apply {
            val id = c.getInt(0)
            val name = c.getString(1)
            val songs = c.getString(2).split(",")

            return Playlist(id, name, songs)
        }
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
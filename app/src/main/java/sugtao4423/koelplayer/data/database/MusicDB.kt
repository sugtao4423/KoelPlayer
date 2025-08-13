package sugtao4423.koelplayer.data.database

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import sugtao4423.koel4j.dataclass.Album
import sugtao4423.koel4j.dataclass.AllMusicData
import sugtao4423.koel4j.dataclass.Artist
import sugtao4423.koel4j.dataclass.Playlist
import sugtao4423.koel4j.dataclass.Song
import java.util.Date

class MusicDB(private val context: Context) {

    companion object {
        const val SQL_SELECT_SONGS = """
            SELECT songs.id, songs.title, songs.length, songs.track, songs.disc, songs.createdAt,
            albums.id AS albumId, albums.name AS albumName, albums.cover AS albumCover, albums.createdAt AS albumCreatedAt, albums.isCompilation AS albumIsCompilation,
            songArtists.id AS songArtistId, songArtists.name AS songArtistName, songArtists.image AS songArtistImage,
            albumArtists.id AS albumArtistId, albumArtists.name AS albumArtistName, albumArtists.image AS albumArtistImage
            FROM songs
            INNER JOIN albums ON songs.albumId = albums.id
            INNER JOIN artists AS songArtists ON songs.artistId = songArtists.id
            INNER JOIN artists AS albumArtists ON albums.artistId = albumArtists.id
        """
        const val SQL_SELECT_ALBUMS = """
            SELECT albums.id, albums.name, albums.cover, albums.createdAt, albums.isCompilation,
            albumArtists.id AS albumArtistId, albumArtists.name AS albumArtistName, albumArtists.image AS albumArtistImage
            FROM albums
            INNER JOIN artists AS albumArtists ON albums.artistId = albumArtists.id
        """
        const val SQL_SELECT_ARTISTS = "SELECT * FROM artists"
        const val SQL_SELECT_PLAYLISTS = "SELECT * FROM playlists"
    }

    private val db = MusicDBHelper(context).writableDatabase

    fun close() {
        db.close()
    }

    fun resetDatabase() {
        val tables = arrayOf("albums", "artists", "songs", "playlists")
        tables.forEach {
            db.execSQL("DROP TABLE $it")
        }
        MusicDBHelper(context).onCreate(db)
    }

    fun getAlbumSongs(albumId: String): List<Song> {
        val sql = "$SQL_SELECT_SONGS WHERE songs.albumId = ?"
        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery(sql, arrayOf(albumId))
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()
        songs.sortBy { it.track }
        return songs
    }

    fun getSongsById(songIds: List<String>): List<Song> {
        val songs = ArrayList<Song>()
        songIds.chunked(500) {
            var sql = "$SQL_SELECT_SONGS WHERE " + " songs.id = ? OR".repeat(it.size)
            sql = sql.removeSuffix("OR")
            val songCursor = db.rawQuery(sql, it.toTypedArray())
            while (songCursor.moveToNext()) {
                songs.add(getSongData(songCursor))
            }
            songCursor.close()
        }
        return songs
    }

    fun getAllMusicData(): AllMusicData {
        val albums = ArrayList<Album>()
        val albumCursor = db.rawQuery(SQL_SELECT_ALBUMS, null)
        while (albumCursor.moveToNext()) {
            albums.add(getAlbumData(albumCursor))
        }
        albumCursor.close()

        val artists = ArrayList<Artist>()
        val artistCursor = db.rawQuery(SQL_SELECT_ARTISTS, null)
        while (artistCursor.moveToNext()) {
            artists.add(getArtistData(artistCursor))
        }
        artistCursor.close()

        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery(SQL_SELECT_SONGS, null)
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()

        val playlists = ArrayList<Playlist>()
        val playlistCursor = db.rawQuery(SQL_SELECT_PLAYLISTS, null)
        while (playlistCursor.moveToNext()) {
            playlists.add(getPlaylistData(playlistCursor))
        }
        playlistCursor.close()

        return AllMusicData(albums, artists, songs, playlists)
    }

    private fun getAlbumData(c: Cursor): Album {
        val albumArtist = c.let {
            val id = it.getString(5)
            val name = it.getString(6)
            val image = it.getStringOrNull(7)
            Artist(id, name, image)
        }
        val id = c.getString(0)
        val name = c.getString(1)
        val cover = c.getStringOrNull(2)
        val createdAt = Date(c.getLong(3))
        val isCompilation = c.getString(4).toBoolean()

        return Album(id, albumArtist, name, cover, createdAt, isCompilation)
    }

    private fun getArtistData(c: Cursor): Artist {
        val id = c.getString(0)
        val name = c.getString(1)
        val image = c.getStringOrNull(2)

        return Artist(id, name, image)
    }

    private fun getSongData(c: Cursor): Song {
        val albumArtist = c.let {
            val id = it.getString(14)
            val name = it.getString(15)
            val image = it.getStringOrNull(16)
            Artist(id, name, image)
        }
        val album = c.let {
            val id = it.getString(6)
            val name = it.getString(7)
            val cover = it.getStringOrNull(8)
            val createdAt = Date(c.getLong(9))
            val isCompilation = c.getString(10).toBoolean()
            Album(id, albumArtist, name, cover, createdAt, isCompilation)
        }
        val songArtist = c.let {
            val id = it.getString(11)
            val name = it.getString(12)
            val image = it.getStringOrNull(13)
            Artist(id, name, image)
        }
        val id = c.getString(0)
        val title = c.getString(1)
        val length = c.getDouble(2)
        val track = c.getInt(3)
        val disc = c.getInt(4)
        val createdAt = Date(c.getLong(5))

        return Song(id, album, songArtist, title, length, track, disc, createdAt)
    }

    private fun getPlaylistData(c: Cursor): Playlist {
        val id = c.getString(0)
        val name = c.getString(1)
        val songs = c.getString(2).split(",")

        return Playlist(id, name, songs)
    }

    fun saveAllMusicData(musicData: AllMusicData) {
        insertAlbumData(musicData.albums)
        insertArtistData(musicData.artists)
        insertSongData(musicData.songs)
        insertPlaylist(musicData.playlists)
    }

    private fun insertAlbumData(albums: List<Album>) {
        val sql = "INSERT INTO albums VALUES (?, ?, ?, ?, ?, ?)"
        albums.forEach {
            db.compileStatement(sql).apply {
                bindString(1, it.id)
                bindString(2, it.artist.id)
                bindString(3, it.name)
                if (it.cover == null) bindNull(4) else bindString(4, it.cover)
                bindLong(5, it.createdAt.time)
                bindString(6, it.isCompilation.toString())
                execute()
                close()
            }
        }
    }

    private fun insertArtistData(artists: List<Artist>) {
        val sql = "INSERT INTO artists VALUES (?, ?, ?)"
        artists.forEach {
            db.compileStatement(sql).apply {
                bindString(1, it.id)
                bindString(2, it.name)
                if (it.image == null) bindNull(3) else bindString(3, it.image)
                execute()
                close()
            }
        }
    }

    private fun insertSongData(songs: List<Song>) {
        val sql = "INSERT INTO songs VALUES(?, ?, ?, ?, ?, ?, ?, ?)"
        songs.forEach {
            db.compileStatement(sql).apply {
                bindString(1, it.id)
                bindString(2, it.album.id)
                bindString(3, it.artist.id)
                bindString(4, it.title)
                bindDouble(5, it.length)
                bindLong(6, it.track.toLong())
                bindLong(7, it.disc.toLong())
                bindLong(8, it.createdAt.time)
                execute()
                close()
            }
        }
    }

    private fun insertPlaylist(playlists: List<Playlist>) {
        val sql = "INSERT INTO playlists VALUES (?, ?, ?)"
        playlists.forEach {
            val bindArgs = arrayOf(
                it.id,
                it.name,
                it.songs.joinToString(","),
            )
            db.compileStatement(sql).apply {
                bindAllArgsAsStrings(bindArgs)
                execute()
                close()
            }
        }
    }

}

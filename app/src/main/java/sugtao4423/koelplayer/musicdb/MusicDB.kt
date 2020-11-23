package sugtao4423.koelplayer.musicdb

import android.content.Context
import android.database.Cursor
import sugtao4423.koel4j.dataclass.*
import java.util.*
import kotlin.collections.ArrayList

class MusicDB(private val context: Context) {

    companion object {
        const val SQL_SELECT_SONGS = "SELECT songs.id, songs.title, songs.length, songs.track, songs.disc, songs.createdAt, " +
                "albums.id AS albumId, albums.name AS albumName, albums.cover AS albumCover, albums.createdAt AS albumCreatedAt, albums.isCompilation AS albumIsCompilation, " +
                "songArtists.id AS songArtistId, songArtists.name AS songArtistName, songArtists.image AS songArtistImage, " +
                "albumArtists.id AS albumArtistId, albumArtists.name AS albumArtistName, albumArtists.image AS albumArtistImage " +
                "FROM songs " +
                "INNER JOIN albums ON songs.albumId = albums.id " +
                "INNER JOIN artists AS songArtists ON songs.artistId = songArtists.id " +
                "INNER JOIN artists AS albumArtists ON albums.artistId = albumArtists.id"
        const val SQL_SELECT_ALBUMS = "SELECT albums.id, albums.name, albums.cover, albums.createdAt, albums.isCompilation, " +
                "albumArtists.id AS albumArtistId, albumArtists.name AS albumArtistName, albumArtists.image AS albumArtistImage " +
                "FROM albums " +
                "INNER JOIN artists AS albumArtists ON albums.artistId = albumArtists.id"
        const val SQL_SELECT_ARTISTS = "SELECT * FROM artists"
        const val SQL_SELECT_PLAYLISTS = "SELECT * FROM playlists"
    }

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
        val sql = "$SQL_SELECT_SONGS WHERE songs.albumId = ?"
        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery(sql, arrayOf(albumId.toString()))
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()
        songs.sortBy { it.track }
        return songs
    }

    fun getSongsById(songIds: List<String>): List<Song> {
        var sql = "$SQL_SELECT_SONGS WHERE " + " songs.id = ? OR".repeat(songIds.size)
        sql = sql.removeSuffix("OR")
        val songs = ArrayList<Song>()
        val songCursor = db.rawQuery(sql, songIds.toTypedArray())
        while (songCursor.moveToNext()) {
            songs.add(getSongData(songCursor))
        }
        songCursor.close()
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
            val id = it.getInt(5)
            val name = it.getString(6)
            val image = it.getString(7)
            Artist(id, name, image)
        }
        val id = c.getInt(0)
        val name = c.getString(1)
        val cover = c.getString(2)
        val createdAt = Date(c.getLong(3) * 1000)
        val isCompilation = c.getString(4).toBoolean()

        return Album(id, albumArtist, name, cover, createdAt, isCompilation)
    }

    private fun getArtistData(c: Cursor): Artist {
        val id = c.getInt(0)
        val name = c.getString(1)
        val image = c.getString(2)

        return Artist(id, name, image)
    }

    private fun getSongData(c: Cursor): Song {
        val albumArtist = c.let {
            val id = it.getInt(14)
            val name = it.getString(15)
            val image = it.getString(16)
            Artist(id, name, image)
        }
        val album = c.let {
            val id = it.getInt(6)
            val name = it.getString(7)
            val cover = it.getString(8)
            val createdAt = Date(c.getLong(9) * 1000)
            val isCompilation = c.getString(10).toBoolean()
            Album(id, albumArtist, name, cover, createdAt, isCompilation)
        }
        val songArtist = c.let {
            val id = it.getInt(11)
            val name = it.getString(12)
            val image = it.getString(13)
            Artist(id, name, image)
        }
        val id = c.getString(0)
        val title = c.getString(1)
        val length = c.getDouble(2)
        val track = c.getInt(3)
        val disc = c.getInt(4)
        val createdAt = Date(c.getLong(5) * 1000)

        return Song(id, album, songArtist, title, length, track, disc, createdAt)
    }

    private fun getPlaylistData(c: Cursor): Playlist {
        val id = c.getInt(0)
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
        albums.map {
            val bindArgs = arrayOf(
                it.id.toString(),
                it.artist.id.toString(),
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
                it.album.id.toString(),
                it.artist.id.toString(),
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
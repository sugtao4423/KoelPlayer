package sugtao4423.koelplayer.musicdb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicDBHelper(context: Context) : SQLiteOpenHelper(context, "MusicDB", null, 1) {

    companion object {
        private const val CREATE_ALBUM = "CREATE TABLE albums (" +
                "id INTEGER, artistId INTEGER, name TEXT, cover TEXT, createdAt INTEGER, isCompilation TEXT)"
        private const val CREATE_ARTIST = "CREATE TABLE artists (" +
                "id INTEGER, name TEXT, image TEXT)"
        private const val CREATE_SONG = "CREATE TABLE songs (" +
                "id TEXT, albumId INTEGER, artistId INTEGER, title TEXT, length REAL, track INTEGER, disc INTEGER, createdAt INTEGER)"
        private const val CREATE_PLAYLIST = "CREATE TABLE playlists (" +
                "id INTEGER, name TEXT, songs TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!
        db.execSQL(CREATE_ALBUM)
        db.execSQL(CREATE_ARTIST)
        db.execSQL(CREATE_SONG)
        db.execSQL(CREATE_PLAYLIST)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}
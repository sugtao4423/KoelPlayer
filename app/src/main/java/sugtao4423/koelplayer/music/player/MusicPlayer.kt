package sugtao4423.koelplayer.music.player

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlin.random.Random

@OptIn(UnstableApi::class)
class MusicPlayer(private val exoPlayer: ExoPlayer) : ForwardingSimpleBasePlayer(exoPlayer) {

    private fun ShuffleOrder.toMutableList(): MutableList<Int> {
        val order = mutableListOf<Int>()
        var index = firstIndex
        repeat(length) {
            order.add(index)
            index = getNextIndex(index)
        }
        return order
    }

    private fun List<Int>.toShuffleOrder(): ShuffleOrder {
        return ShuffleOrder.DefaultShuffleOrder(this.toIntArray(), Random.nextLong())
    }

    private var isShuffle = false

    override fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>): ListenableFuture<*> {
        if (!isShuffle || mediaItems.isEmpty()) {
            return super.handleAddMediaItems(index, mediaItems)
        }

        val shuffleOrder = exoPlayer.shuffleOrder.toMutableList()
        for (i in shuffleOrder.indices) {
            if (shuffleOrder[i] >= index) {
                shuffleOrder[i] += mediaItems.size
            }
        }

        val newIndices = (index until index + mediaItems.size).toList()
        val insertShufflePosition = shuffleOrder.indexOf(index - 1) + 1
        shuffleOrder.addAll(insertShufflePosition, newIndices)

        super.handleAddMediaItems(index, mediaItems)
        exoPlayer.shuffleOrder = shuffleOrder.toShuffleOrder()
        return Futures.immediateVoidFuture()
    }

    override fun handleMoveMediaItems(
        fromIndex: Int, toIndex: Int, newIndex: Int
    ): ListenableFuture<*> {
        if (!isShuffle) {
            return super.handleMoveMediaItems(fromIndex, toIndex, newIndex)
        }

        val shuffleOrder = exoPlayer.shuffleOrder.toMutableList()
        val moveItems = shuffleOrder.subList(fromIndex, toIndex).toList()
        (fromIndex until toIndex).forEach {
            shuffleOrder.removeAt(it)
        }
        shuffleOrder.addAll(newIndex, moveItems)

        exoPlayer.shuffleOrder = shuffleOrder.toShuffleOrder()
        return Futures.immediateVoidFuture()
    }

    override fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int): ListenableFuture<*> {
        if (!isShuffle) {
            return super.handleRemoveMediaItems(fromIndex, toIndex)
        }

        (fromIndex until toIndex).forEach {
            val shuffleOrder = exoPlayer.shuffleOrder.toMutableList()
            val index = shuffleOrder[it]
            exoPlayer.removeMediaItem(index)
        }
        return Futures.immediateVoidFuture()
    }

    // No need to override for the this project
    override fun handleReplaceMediaItems(
        fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>
    ): ListenableFuture<*> {
        return super.handleReplaceMediaItems(fromIndex, toIndex, mediaItems)
    }

    override fun handleSeek(
        mediaItemIndex: Int, positionMs: Long, seekCommand: Int
    ): ListenableFuture<*> {
        if (!isShuffle) {
            return super.handleSeek(mediaItemIndex, positionMs, seekCommand)
        }

        val shuffleOrder = exoPlayer.shuffleOrder.toMutableList()
        val index = shuffleOrder[mediaItemIndex]
        return super.handleSeek(index, positionMs, seekCommand)
    }

    // No need to override for the this project
    override fun handleSetMediaItems(
        mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long
    ): ListenableFuture<*> {
        return super.handleSetMediaItems(mediaItems, startIndex, startPositionMs)
    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean): ListenableFuture<*> {
        isShuffle = shuffleModeEnabled
        return super.handleSetShuffleModeEnabled(shuffleModeEnabled)
    }

}

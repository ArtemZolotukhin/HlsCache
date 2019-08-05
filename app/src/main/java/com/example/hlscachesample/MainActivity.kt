package com.example.hlscachesample

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handler = Handler()
        val exoPlayer = ExoPlayerFactory.newSimpleInstance(this)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerView.player = exoPlayer
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.showController()

        val evictor = LeastRecentlyUsedCacheEvictor(1024 * 1024 * 32)
        val cache = SimpleCache(File(this.cacheDir, "example"), evictor, ExoDatabaseProvider(this))
        val dataSourceFactory = CacheDataSourceFactory(cache, DefaultHttpDataSourceFactory("example_user_agent", null))

        val precacher = SimpleVideoPreCacher(cache, dataSourceFactory)

//        val firstSource = "https://asazin-cache.cdnvideo.ru/asazin/interactive/com.movika.tutorial/240p/7DA89013-2DEB-49AC-B892-2AFD7C7BE205_240p.mp4"
//        val secondSource = "https://asazin-cache.cdnvideo.ru/asazin/interactive/com.movika.tutorial/240p/FF57313A-658E-4B3E-9D15-7F09F69EB81C_240p.mp4"
        val firstSource = "https://asazin-cache.cdnvideo.ru/asazin/test/hls/395.m3u8"
        val secondSource = "https://asazin-cache.cdnvideo.ru/asazin/test/hls/418.m3u8"

        val concatenatingMediaSource = ConcatenatingMediaSource()
        concatenatingMediaSource.addMediaSource(createMediaSource(firstSource, dataSourceFactory), handler) {
            exoPlayer.prepare(concatenatingMediaSource, true, true)
            precacher.preCache(Uri.parse(secondSource))
            handler.postDelayed({
                concatenatingMediaSource.addMediaSource(createMediaSource(secondSource, dataSourceFactory))
            }, 5000)
        }

        exoPlayer.prepare(concatenatingMediaSource, true, true)
    }

    private fun createMediaSource(source: String, dataSourceFactory: CacheDataSourceFactory): MediaSource {
        return createMediaSourceFactory(source, dataSourceFactory)
            .createMediaSource(Uri.parse(source))
    }

    private fun createMediaSourceFactory(
        source: String,
        dataSourceFactory: CacheDataSourceFactory,
        tag: Any? = null
    ): AdsMediaSource.MediaSourceFactory {
        val extension = source.substringAfterLast('.', "")
        return if (extension == "m3u8" || extension == "m3u") {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setTag(tag)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).setTag(tag)
        }
    }
}

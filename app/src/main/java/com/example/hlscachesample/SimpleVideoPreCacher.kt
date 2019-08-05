package com.example.hlscachesample

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import java.util.concurrent.Executors

open class SimpleVideoPreCacher(
    private val cache: Cache,
    private val dataSourceFactory: CacheDataSourceFactory,
    threadCount: Int = DEFAULT_THREADS_COUNT
) : VideoPreCacher {

    companion object {
        /** Means 3 seconds */
        const val DEFAULT_CACHE_LENGTH = 3L * 1000L
        const val DEFAULT_THREADS_COUNT = 4
    }

    private val executorService = Executors.newFixedThreadPool(threadCount)

    override fun preCache(uri: Uri, position: Long) {
        val dataSpec = DataSpec(uri, position, DEFAULT_CACHE_LENGTH, null)
        val dataSource = dataSourceFactory.createDataSource()
        cache(dataSpec, cache, dataSource)
        log("cache url - $uri")
    }

    private fun cache(
        dataSpec: DataSpec,
        cache: Cache,
        dataSource: DataSource
    ) {
        executorService.execute {
            try {
                CacheUtil.cache(
                    dataSpec,
                    cache,
                    null,
                    dataSource,
                    CacheUtil.ProgressListener { requestLength, bytesCached, newBytesCached ->
                        log("progressListener: $requestLength $bytesCached $newBytesCached")
                    },
                    null
                )
            } catch (e: HttpDataSource.InvalidResponseCodeException) {
                e.printStackTrace()
            } catch (e: DataSourceException) {
                e.printStackTrace()
            } catch (e: HttpDataSource.HttpDataSourceException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun log(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d("VideoPreCacher", msg)
        }
    }

    private fun logW(msg: String) {
        Log.w("VideoPreCacher", msg)
    }
}
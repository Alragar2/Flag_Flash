// Crea un archivo llamado MyAppGlideModule.kt
package alragar2.isi3.uv.flagflash

import android.content.Context
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Configurar opciones de caché
        builder.setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cachear todas las versiones de la imagen
        )

        // Configurar caché de memoria
        val memoryCacheSizeBytes = (1024 * 1024 * 20).toLong() // 20 MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))

        // Configurar caché de disco
        val diskCacheSizeBytes = (1024 * 1024 * 100).toLong() // 100 MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))
    }
}
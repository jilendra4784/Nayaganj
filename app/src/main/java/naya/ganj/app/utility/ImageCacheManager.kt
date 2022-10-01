package naya.ganj.app.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.collection.LruCache
import java.net.URL

class ImageCacheManager private constructor() {
    private object HOLDER {
        val INSTANCE = ImageCacheManager()
    }

    companion object {
        val instance: ImageCacheManager by lazy { HOLDER.INSTANCE }
    }

    val lru: LruCache<Any, Any> = LruCache(1024)
    fun saveBitmapToCahche(key: String, bitmap: Bitmap) {
        try {
            instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }
    }

    fun retrieveBitmapFromCache(key: String): Bitmap? {
        try {
            return instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
        }

        return null
    }

    fun loadCacheImage(imageView: ImageView, imageURL: String) {
        val imageBitmap = retrieveBitmapFromCache(imageURL)
        if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap)
        } else {
            try {
                Thread {
                    val url = URL(imageURL)
                    val newBitmap =
                        BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    Handler(Looper.getMainLooper()).post {
                        if (newBitmap != null) {
                            imageView.setImageBitmap(newBitmap)
                            saveBitmapToCahche(imageURL, newBitmap)
                        }
                    }
                }.start()
            } catch (e: Exception) {
                System.out.println(e)
            }
        }
    }
}
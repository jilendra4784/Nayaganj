package naya.ganj.app.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

class ImageCacheManager private constructor() {
    private object HOLDER {
        val INSTANCE = ImageCacheManager()
    }

    companion object {
        val instance: ImageCacheManager by lazy { HOLDER.INSTANCE }
    }

      val lru: LruCache<Any, Any> = LruCache(1024)
    fun saveBitmapToCahche(context: Context, key: String, bitmap: Bitmap) {
        var fileOutputStream: FileOutputStream? = null
        try {
            instance.lru.put(key, bitmap)

           /* val cacheDir: File = context.cacheDir
            val file = File(cacheDir, key)
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)*/

        } catch (e: Exception) {
        } finally {
            fileOutputStream?.close()
        }
    }

    fun retrieveBitmapFromCache(context: Context, key: String): Bitmap? {
        try {
             return instance.lru.get(key) as Bitmap?

           /* val cacheDir: File = context.cacheDir
            val f = File(cacheDir, key)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(f)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            val bitmap = BitmapFactory.decodeStream(fis)
            return bitmap*/
        } catch (e: Exception) {
        }

        return null
    }

    fun loadCacheImage(context: Context, imageView: ImageView, imageURL: String) {
        val imageBitmap = retrieveBitmapFromCache(context, imageURL)
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
                            saveBitmapToCahche(context, imageURL, newBitmap)
                        }
                    }
                }.start()
            } catch (e: Exception) {
                System.out.println(e)
            }
        }
    }
}
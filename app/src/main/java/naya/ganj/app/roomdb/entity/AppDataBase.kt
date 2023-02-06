package naya.ganj.app.roomdb.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductDetail::class, SavedAmountModel::class,CartModel::class,ReturnProduct::class,RecentSuggestion::class], version = 1)
abstract class AppDataBase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java,
                        "product_db"
                    ).build()

                }
            }
            return INSTANCE!!
        }
    }

}
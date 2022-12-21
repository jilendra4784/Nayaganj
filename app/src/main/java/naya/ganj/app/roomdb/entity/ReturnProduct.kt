package naya.ganj.app.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ReturnProduct(val productId: String,
                    val variantId: String,
                    var itemQuantity: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

}
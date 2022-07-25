package naya.ganj.app.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CartModel(
    val productId: String,
    val variantId: String,
    var itemQuantity: Int,
    val cartAmount: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}


package naya.ganj.app.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProductDetail(
    val productId: String,
    val variantId: String,
    var itemQuantity: Int,
    val productName: String,
    val imageUrl: String,
    val vPrice: Double,
    val vDiscount: Int,
    val vUnitQuantity: String,
    val vUnit: String,
    val totalVariantQuantity: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}

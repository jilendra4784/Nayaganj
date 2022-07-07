package naya.ganj.app.roomdb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProductDetail(
    @ColumnInfo(name = "product_id") val productid: String,
    @ColumnInfo(name = "variant_id") val variantid: Int,
    @ColumnInfo(name = "total_amount") val totalAmount: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}

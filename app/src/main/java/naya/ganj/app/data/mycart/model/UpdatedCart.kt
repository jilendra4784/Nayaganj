package naya.ganj.app.data.mycart.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdatedCart(
    @SerializedName("actualPrice")
    val actualPrice: Double,
    @SerializedName("actualPriceAfterPromoCode")
    val actualPriceAfterPromoCode: Double,
    @SerializedName("created")
    val created: String,
    @SerializedName("discountPrice")
    val discountPrice: Double,
    @SerializedName("price")
    val price: Int,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("variantId")
    val variantId: String
) : Parcelable
package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class CouponResponseModel(
    @SerializedName("codeName")
    val codeName: String,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("offer")
    val offer: String,
    @SerializedName("promoCodeDiscountAmount")
    val promoCodeDiscountAmount: Double,
    @SerializedName("promoCodeId")
    val promoCodeId: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("updatedCartList")
    val updatedCartList: List<UpdatedCart>
) {
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
    )
}
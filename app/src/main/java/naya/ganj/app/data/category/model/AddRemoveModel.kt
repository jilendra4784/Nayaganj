package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName

data class AddRemoveModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("promoCodeDiscountAmount")
    val promoCodeDiscountAmount: Int,
    @SerializedName("status")
    val status: Boolean
)
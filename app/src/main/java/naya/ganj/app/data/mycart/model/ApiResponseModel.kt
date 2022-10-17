package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class ApiResponseModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("promoCodeDiscountAmount")
    val promoCodeDiscountAmount: Double,
)
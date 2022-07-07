package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName

data class CheckProductInCartModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("productQuantity")
    val productQuantity: Int,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("userId")
    val userId: String
)
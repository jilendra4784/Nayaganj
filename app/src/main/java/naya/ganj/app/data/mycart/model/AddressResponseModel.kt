package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class AddressResponseModel(
    @SerializedName("addressId")
    val addressId: String,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean
)
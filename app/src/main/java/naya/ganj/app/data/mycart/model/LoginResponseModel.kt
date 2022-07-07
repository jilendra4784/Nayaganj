package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class LoginResponseModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean
)
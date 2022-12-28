package naya.ganj.app.data.sidemenu.model


import com.google.gson.annotations.SerializedName

data class MyWalletModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("walletBalance")
    val walletBalance: Int
)
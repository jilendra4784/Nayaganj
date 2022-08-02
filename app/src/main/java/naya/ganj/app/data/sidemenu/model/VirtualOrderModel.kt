package naya.ganj.app.data.sidemenu.model


import com.google.gson.annotations.SerializedName

data class VirtualOrderModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("virtualOrdersList")
    val virtualOrdersList: List<VirtualOrders>
) {
    data class VirtualOrders(
        @SerializedName("date")
        val date: String,
        @SerializedName("fileName")
        val fileName: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("virtualOrderId")
        val virtualOrderId: String
    )
}
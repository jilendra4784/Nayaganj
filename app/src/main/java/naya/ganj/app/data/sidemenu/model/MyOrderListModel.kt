package naya.ganj.app.data.sidemenu.model


import com.google.gson.annotations.SerializedName

data class MyOrderListModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("ordersList")
    val ordersList: List<Orders>
) {
    data class Orders(
        @SerializedName("date")
        val date: String,
        @SerializedName("orderId")
        val orderId: String,
        @SerializedName("orderStatus")
        val orderStatus: String,
        @SerializedName("totalAmount")
        val totalAmount: Double,
        @SerializedName("totalItems")
        val totalItems: Int
    )
}
package naya.ganj.app.deliverymodule.model


import com.google.gson.annotations.SerializedName

data class DeliveredOrdersModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("ordersList")
    val ordersList: List<Orders>
) {
    data class Orders(
        @SerializedName("address")
        val address: Address,
        @SerializedName("date")
        val date: String,
        @SerializedName("orderId")
        val orderId: String,
        @SerializedName("orderStatus")
        val orderStatus: String,
        @SerializedName("paymentStatus")
        val paymentStatus: String,
        @SerializedName("totalAmount")
        val totalAmount: Double,
        @SerializedName("totalItems")
        val totalItems: Int
    ) {
        data class Address(
            @SerializedName("ApartName")
            val apartName: String,
            @SerializedName("city")
            val city: String,
            @SerializedName("contactNumber")
            val contactNumber: String,
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("houseNo")
            val houseNo: String,
            @SerializedName("landmark")
            val landmark: String,
            @SerializedName("lastName")
            val lastName: String,
            @SerializedName("nickName")
            val nickName: String,
            @SerializedName("pincode")
            val pincode: String,
            @SerializedName("street")
            val street: String
        )
    }
}
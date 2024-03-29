package naya.ganj.app.deliverymodule.model


import com.google.gson.annotations.SerializedName

data class DeliveryOrderDetailModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("orderDetails")
    val orderDetails: OrderDetails,
    @SerializedName("status")
    val status: Boolean
) {
    data class OrderDetails(
        @SerializedName("address")
        val address: Address,
        @SerializedName("created")
        val created: String,
        @SerializedName("deliverCharges")
        val deliverCharges: Int,
        @SerializedName("deliveryBoyId")
        val deliveryBoyId: String,
        @SerializedName("deliveryReAttempt")
        val deliveryReAttempt: Int,
        @SerializedName("_id")
        val id: String,
        @SerializedName("itemTotal")
        val itemTotal: Double,
        @SerializedName("lastUpdated")
        val lastUpdated: String,
        @SerializedName("loc")
        val loc: Loc,
        @SerializedName("orderNo")
        val orderNo: Int,
        @SerializedName("orderStatus")
        val orderStatus: String,
        @SerializedName("paymentMode")
        val paymentMode: String,
        @SerializedName("paymentModeByDelBoy")
        val paymentModeByDelBoy: String,
        @SerializedName("paymentOrderId")
        val paymentOrderId: String,
        @SerializedName("paymentRecieveByDelBoy")
        val paymentRecieveByDelBoy: Double,
        @SerializedName("paymentStatus")
        val paymentStatus: String,
        @SerializedName("buttonIndex")
        val buttonIndex: String,
        @SerializedName("products")
        val products: List<Product>,
        @SerializedName("totalAmount")
        val totalAmount: Double,
        @SerializedName("refundedAmount")
       val refundedAmount: Double
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

        data class Loc(
            @SerializedName("coordinates")
            val coordinates: List<Any>,
            @SerializedName("type")
            val type: String
        )
    }
}
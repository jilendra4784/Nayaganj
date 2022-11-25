package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class MyCartModel(
    @SerializedName("address")
    val address: Address,
    @SerializedName("cartList")
    val cartList: MutableList<Cart>,
    @SerializedName("deliveryCharges")
    val deliveryCharges: Int,
    @SerializedName("deliveryChargesThreshHold")
    val deliveryChargesThreshHold: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("_id")
    val addressId: String,
    @SerializedName("walletBalance")
    val walletBalance: Int


) {
    data class Address(
        @SerializedName("address")
        val address: Address,
        @SerializedName("created")
        val created: String,
        @SerializedName("default")
        val default: Boolean,
        @SerializedName("_id")
        val id: String,
        @SerializedName("lastUpdated")
        val lastUpdated: String,
        @SerializedName("loc")
        val loc: Loc,
        @SerializedName("userId")
        val userId: String
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

    data class Cart(
        @SerializedName("actualPrice")
        val actualPrice: String,
        @SerializedName("categoryId")
        val categoryId: String,
        @SerializedName("created")
        val created: String,
        @SerializedName("discountPrice")
        val discountPrice: String,
        @SerializedName("img")
        val img: String,
        @SerializedName("price")
        val price: Int,
        @SerializedName("productBrand")
        val productBrand: String,
        @SerializedName("productId")
        val productId: String,
        @SerializedName("productName")
        val productName: String,
        @SerializedName("quantity")
        val quantity: Int,
        @SerializedName("variantId")
        val variantId: String,
        @SerializedName("variantQuantity")
        val variantQuantity: Int,
        @SerializedName("variantType")
        val variantType: String,
        @SerializedName("variantUnit")
        val variantUnit: String,
        @SerializedName("variantUnitQuantity")
        val variantUnitQuantity: String
    )
}
package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName

data class ProductDetailModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("productDetails")
    val productDetails: ProductDetails
) {
    data class ProductDetails(
        @SerializedName("brand")
        val brand: String,
        @SerializedName("categoryId")
        val categoryId: List<String>,
        @SerializedName("created")
        val created: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("enable")
        val enable: Boolean,
        @SerializedName("features")
        val features: Any,
        @SerializedName("howToUse")
        val howToUse: Any,
        @SerializedName("_id")
        val id: String,
        @SerializedName("imgUrl")
        val imgUrl: List<String>,
        @SerializedName("ingredients")
        val ingredients: Any,
        @SerializedName("otherInfo")
        val otherInfo: Any,
        @SerializedName("productName")
        val productName: String,
        @SerializedName("variant")
        val variant: List<Variant>
    ) {
        data class Variant(
            @SerializedName("vCreated")
            val vCreated: String,
            @SerializedName("vDiscount")
            val vDiscount: Int,
            @SerializedName("vEnable")
            val vEnable: Boolean,
            @SerializedName("vId")
            val vId: String,
            @SerializedName("vPrice")
            val vPrice: Double,
            @SerializedName("vQuantity")
            val vQuantity: Int,
            @SerializedName("vQuantityInCart")
            val vQuantityInCart: Int,
            @SerializedName("vType")
            val vType: String,
            @SerializedName("vUnit")
            val vUnit: String,
            @SerializedName("vUnitQuantity")
            val vUnitQuantity: String
        )
    }
}
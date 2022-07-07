package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName

data class ProductListModel(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("productList")
    val productList: List<Product>,
    @SerializedName("status")
    val status: Boolean
) {
    data class Product(
        @SerializedName("brand")
        val brand: String,
        @SerializedName("categoryId")
        val categoryId: List<String>,
        @SerializedName("description")
        val description: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("imgUrl")
        val imgUrl: List<String>,
        @SerializedName("productName")
        val productName: String,
        @SerializedName("variant")
        val variant: List<Variant>
    ) {
        data class Variant(
            @SerializedName("vDiscount")
            val vDiscount: Int,
            @SerializedName("vId")
            val vId: String,
            @SerializedName("vPrice")
            val vPrice: Double,
            @SerializedName("vQuantity")
            val vQuantity: Int,
            @SerializedName("vUnit")
            val vUnit: String,
            @SerializedName("vUnitQuantity")
            val vUnitQuantity: Int,
            @SerializedName("vQuantityInCart")
            val vQuantityInCart: Int
        )
    }
}
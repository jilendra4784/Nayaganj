package naya.ganj.app.data.home.model


import com.google.gson.annotations.SerializedName

data class HomePageModel(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("status")
    val status: Boolean
) {
    data class Data(
        @SerializedName("category")
        val category: List<Category>,
        @SerializedName("itemsInCart")
        val itemsInCart: Int,
        @SerializedName("OfferPromoBanner")
        val offerPromoBanner: List<OfferPromoBanner>,
        @SerializedName("productList")
        val productList: List<Product>,
        @SerializedName("productName")
        val productName: String,
        @SerializedName("promoBanner")
        val promoBanner: List<PromoBanner>,
        @SerializedName("status")
        val status: Boolean,
        @SerializedName("totalAmount")
        val totalAmount: Int,
        @SerializedName("totalAmountAfterDis")
        val totalAmountAfterDis: Int,
        @SerializedName("subCategoryList")
        val subCategoryList: List<Category>,
        @SerializedName("subCategoryName")
        val subCategoryName: String,
        @SerializedName("brandList")
        val brandList: List<Brand>,


        ) {
        data class Category(
            @SerializedName("category")
            val category: String,
            @SerializedName("enable")
            val enable: Boolean,
            @SerializedName("_id")
            val id: String,
            @SerializedName("parentId")
            val parentId: String,
            @SerializedName("time")
            val time: String
        )

        data class OfferPromoBanner(
            @SerializedName("enable")
            val enable: Boolean,
            @SerializedName("_id")
            val id: String,
            @SerializedName("img")
            val img: String,
            @SerializedName("type")
            val type: String
        )

        data class Product(
            @SerializedName("brand")
            val brand: String,
            @SerializedName("categoryId")
            val categoryId: List<String>,
            @SerializedName("description")
            val description: Any?,
            @SerializedName("_id")
            val id: String,
            @SerializedName("imgUrl")
            val imgUrl: List<Any>,
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
                val vPrice: Int,
                @SerializedName("vPriority")
                val vPriority: Int,
                @SerializedName("vQuantity")
                val vQuantity: Int,
                @SerializedName("vQuantityInCart")
                val vQuantityInCart: Int,
                @SerializedName("vUnit")
                val vUnit: String,
                @SerializedName("vUnitQuantity")
                val vUnitQuantity: String
            )
        }

        data class PromoBanner(
            @SerializedName("enable")
            val enable: Boolean,
            @SerializedName("_id")
            val id: String,
            @SerializedName("img")
            val img: String,
            @SerializedName("type")
            val type: String
        )

        data class Brand(
            @SerializedName("_id")
            val id: String,
            @SerializedName("brand")
            val brand: String,
            @SerializedName("imgUrl")
            val imgUrl: String
        )
    }
}
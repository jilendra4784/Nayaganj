package naya.ganj.app.data.category.model


import com.google.gson.annotations.SerializedName

data class CategoryDataModel(
    @SerializedName("categoryList")
    val categoryList: List<Category>,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean
) {
    data class Category(
        @SerializedName("category")
        val category: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("parentId")
        val parentId: String,
        @SerializedName("subCategoryList")
        val subCategoryList: List<SubCategory>
    ) {
        data class SubCategory(
            @SerializedName("category")
            val category: String,
            @SerializedName("_id")
            val id: String,
            @SerializedName("parentId")
            val parentId: String
        )
    }
}
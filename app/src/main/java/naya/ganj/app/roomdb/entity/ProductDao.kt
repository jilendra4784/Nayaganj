package naya.ganj.app.roomdb.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {

    @Insert
    fun insert(productDetail: ProductDetail)

    @Query("SELECT * FROM productdetail")
    fun getProductList(): MutableList<ProductDetail>

    @Query("SELECT * FROM productdetail where productId=:productId ")
    fun getProductListByProductId(productId: String): List<ProductDetail>

    @Query("Select * from productdetail  where productId=:productId and variantId=:variantId")
    fun getSingleProduct(productId: String, variantId: String): ProductDetail

    @Query("update productdetail set itemQuantity =:itemQuantity where productId=:productId and variantId=:variantId")
    fun updateProduct(itemQuantity: Int, productId: String, variantId: String)

    @Query("delete from productdetail where productId=:productId and variantId=:variantId")
    fun deleteProduct(productId: String, variantId: String)

    @Query("SELECT EXISTS(SELECT * FROM productdetail WHERE productid = :proId and variantId=:variantId)")
    fun isProductExist(proId: String, variantId: String): Boolean

    @Query("DELETE FROM productdetail")
    fun deleteAllProduct()

    @Query("update productdetail set itemQuantity=:quantity ,productName=:pName,imageUrl=:pImage, vPrice=:pPrice,vDiscount=:pDiscount,vUnitQuantity=:vUQuantity,vUnit=:pUnit,totalVariantQuantity=:totalQuantity where productId=:productId and variantId=:variantId")
    fun syncCart(
        quantity: Int,
        pName: String,
        pImage: String,
        pPrice: Double,
        pDiscount: Int,
        vUQuantity: String,
        pUnit: String,
        totalQuantity: Int,
        productId: String,
        variantId: String
    )

    // Saved Amount
    @Insert
    fun insertSavedAmount(savedAmountModel: SavedAmountModel)

    @Query("SELECT * FROM savedamountmodel")
    fun getSavedAmountList(): List<SavedAmountModel>

    @Query("update savedamountmodel set amount=:amount where product_id=:productId and variant_id=:variantId")
    fun updateSavedAmount(productId: String, variantId: Int, amount: Double)

    @Query("delete from savedamountmodel where product_id=:productId and variant_id=:variantId")
    fun deleteSavedAmount(productId: String, variantId: Int)

    @Query("DELETE FROM savedamountmodel")
    fun deleteAllSavedAmount()

    @Query("SELECT EXISTS(SELECT * FROM savedamountmodel WHERE product_id = :productId and variant_id=:variantId)")
    fun isSavedItemIsExist(productId: String, variantId: Int): Boolean

    // Cart Model

    @Insert
    fun insertCartDetail(cartModel: CartModel)

    @Query("SELECT * FROM cartmodel")
    fun getCartItemList(): List<CartModel>

    @Query("update cartmodel set cartAmount=:amount where productId=:productId and variantId=:variantId")
    fun updateCart(productId: String, variantId: String, amount: Double)

    @Query("delete from CartModel where productId=:productId and variantId=:variantId")
    fun deleteCartItem(productId: String, variantId: String)

    @Query("DELETE FROM cartmodel")
    fun deleteAllCartData()

    @Query("SELECT EXISTS(SELECT * FROM cartmodel WHERE productId = :productId and variantId=:variantId)")
    fun isCartItemIsExist(productId: String, variantId: Int): Boolean

    // Return Product Detail

    @Insert
    fun insertReturnProduct(returnProduct: ReturnProduct)

    @Query("SELECT * FROM returnproduct")
    fun getReturnProductList(): List<ReturnProduct>

    @Query("delete from returnproduct where productId=:productId and variantId=:variantId")
    fun deleteReturnProduct(productId: String, variantId: String)

    @Query("DELETE FROM returnproduct")
    fun deleteAllReturnItem()

    @Query("SELECT EXISTS(SELECT * FROM returnproduct WHERE productId = :productId and variantId=:variantId)")
    fun isReturnExist(productId: String, variantId: String): Boolean

    @Query("update returnproduct set itemQuantity=:quantiy where productId=:productId and variantId=:variantId")
    fun updateReturnProduct(productId: String, variantId: String, quantiy: Int)

    // Recent Suggestion

    @Insert
    fun insertRecentQuery(recentSuggestion: RecentSuggestion)

    @Query("SELECT * FROM recentsuggestion")
    fun getSuggestionList(): MutableList<RecentSuggestion>

    @Query("delete from RecentSuggestion where `query`=:query ")
    fun deleteSuggesiton(query: String)


    @Query("SELECT EXISTS(SELECT * FROM RecentSuggestion WHERE `query` = :query)")
    fun isSuggestionExist(query: String): Boolean

}
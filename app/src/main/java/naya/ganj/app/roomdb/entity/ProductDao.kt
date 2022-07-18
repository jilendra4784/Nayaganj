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
    fun updateProduct(itemQuantity:Int,productId: String,variantId: String)

    @Query("delete from productdetail where productId=:productId and variantId=:variantId")
    fun deleteProduct(productId: String, variantId: String)

    @Query("SELECT EXISTS(SELECT * FROM productdetail WHERE productid = :proId and variantId=:variantId)")
    fun isProductExist(proId: String, variantId: String): Boolean

    @Query("DELETE FROM productdetail")
    fun deleteAllProduct()

    // Saved Amount
    @Insert
    fun insertSavedAmount(savedAmountModel: SavedAmountModel)

    @Query("SELECT * FROM savedamountmodel")
    fun getSavedAmountList(): List<SavedAmountModel>

    @Query("update savedamountmodel set amount=:amount where product_id=:productId and variant_id=:variantId")
    fun updateAmount(amount: Double, productId: String, variantId: Int)

    @Query("delete from savedamountmodel where product_id=:productId and variant_id=:variantId")
    fun deleteAmount(productId: String, variantId: Int)

    @Query("DELETE FROM savedamountmodel")
    fun deleteAllSavedAmount()

    @Query("SELECT EXISTS(SELECT * FROM savedamountmodel WHERE product_id = :productId and variant_id=:variantId)")
    fun isSavedItemIsExist(productId: String, variantId: Int): Boolean


}
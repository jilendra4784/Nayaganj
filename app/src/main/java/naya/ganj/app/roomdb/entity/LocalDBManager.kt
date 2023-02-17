package naya.ganj.app.roomdb.entity

import android.content.Context
import android.os.Looper
import naya.ganj.app.data.category.view.ProductListActivity
import java.util.logging.Handler

class LocalDBManager {
    companion object{
       fun insertItemInLocal( context: Context, productDetail: ProductDetail){
            Thread{
                AppDataBase.getInstance(context).productDao().insert(productDetail)
            }.start()
        }

        fun updateProduct(context: Context,productDetail: ProductDetail){
            Thread{
                AppDataBase.getInstance(context)
                    .productDao().updateProduct(
                        productDetail.itemQuantity,
                        productDetail.productId,
                        productDetail.variantId
                    )
            }.start()

        }

        fun deleteProduct(context: Context,productDetail: ProductDetail){
            Thread{
                AppDataBase.getInstance(context)
                    .productDao().deleteProduct(
                        productDetail.productId, productDetail.variantId
                    )
            }.start()

        }
    }
}
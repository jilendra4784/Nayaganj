package naya.ganj.app.data.category.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.data.category.repositry.ProductDetailRepositry
import com.google.gson.JsonObject

class ProductDetailViewModel : ViewModel() {

    private val repo = ProductDetailRepositry()
    fun getProductDetai(context: Context,jsonObject: JsonObject) :LiveData<ProductDetailModel> {
       return repo.getProductDetailData(context,jsonObject)
    }
}
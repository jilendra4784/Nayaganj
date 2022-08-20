package naya.ganj.app.data.category.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.model.ProductListModel
import naya.ganj.app.data.category.repositry.ProductListRepositry
import com.google.gson.JsonObject

class ProductListViewModel : ViewModel() {

    var repositry = ProductListRepositry()

    fun getProductList(
        context: Context,
        userId: String?,
        deviceType: String,
        jsonObject: JsonObject
    ): LiveData<ProductListModel> {
        return repositry.getProductList(context,userId, deviceType, jsonObject)
    }

}
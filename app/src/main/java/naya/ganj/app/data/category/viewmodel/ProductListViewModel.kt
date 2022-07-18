package naya.ganj.app.data.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.model.ProductListModel
import naya.ganj.app.data.category.repositry.ProductListRepositry
import com.google.gson.JsonObject

class ProductListViewModel : ViewModel() {

    var repositry = ProductListRepositry()

    fun getProductList(
        userId: String?,
        deviceType: String,
        jsonObject: JsonObject
    ): LiveData<ProductListModel> {
        return repositry.getProductList(userId, deviceType, jsonObject)
    }

    fun addremoveItemRequest(jsonObject: JsonObject):LiveData<AddRemoveModel> {
       return repositry.addremoveItemRequest(jsonObject)
    }

}
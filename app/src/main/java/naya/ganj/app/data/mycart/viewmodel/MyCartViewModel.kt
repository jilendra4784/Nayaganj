package naya.ganj.app.data.mycart.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.repositry.MyCartRepositry
import naya.ganj.app.utility.NetworkResult

class MyCartViewModel : ViewModel() {

    val mutableLiveData = MutableLiveData<NetworkResult<AddRemoveModel>>()

    var myCartRepositry = MyCartRepositry()

    fun getMyCartData(
        context: Context,
        userId: String,
        jsonObject: JsonObject
    ): LiveData<MyCartModel> {
        return myCartRepositry.getMyCartData(context, userId, jsonObject)
    }

}
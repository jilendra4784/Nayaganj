package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.repositry.MyCartRepositry
import com.google.gson.JsonObject

class MyCartViewModel : ViewModel() {

    var myCartRepositry = MyCartRepositry()

    fun getMyCartData(userId:String,jsonObject: JsonObject): LiveData<MyCartModel> {
        return myCartRepositry.getMyCartData(userId,jsonObject)
    }
}
package naya.ganj.app.data.mycart.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.repositry.MyCartRepositry
import com.google.gson.JsonObject

class MyCartViewModel : ViewModel() {

    var myCartRepositry = MyCartRepositry()

    fun getMyCartData(context: Context,userId:String,jsonObject: JsonObject): LiveData<MyCartModel> {
        return myCartRepositry.getMyCartData(context,userId,jsonObject)
    }
}
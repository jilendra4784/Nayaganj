package naya.ganj.app.data.home.repositry

import com.google.gson.JsonObject
import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant

class HomeRepositry(val api: ApiInterface) {

    suspend fun getHomeData(userId:String?, jsonObject: JsonObject) = api.getHomeData(userId, Constant.DEVICE_TYPE,jsonObject)
}
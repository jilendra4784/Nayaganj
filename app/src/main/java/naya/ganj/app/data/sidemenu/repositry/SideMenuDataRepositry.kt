package naya.ganj.app.data.sidemenu.repositry

import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject

class SideMenuDataRepositry(val api : ApiInterface) {

   suspend fun getMyOrderList(userid:String)=api.getMyOrderList(userid,Constant.DEVICE_TYPE)

   suspend fun getOrderDetailRequest(userId:String,jsonObject: JsonObject) =api.getOrderDetailRequest(userId,Constant.DEVICE_TYPE,jsonObject)
}
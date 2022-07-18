package naya.ganj.app.data.sidemenu.repositry

import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject

class SideMenuDataRepositry(val api : ApiInterface) {

   suspend fun getMyOrderList()=api.getMyOrderList("",Constant.DEVICE_TYPE)

   suspend fun getOrderDetailRequest(jsonObject: JsonObject) =api.getOrderDetailRequest("",Constant.DEVICE_TYPE,jsonObject)
}
package naya.ganj.app.data.mycart.repositry

import com.google.gson.JsonObject
import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import okhttp3.RequestBody
import org.json.JSONObject

class AddressListRespositry(var apiInterface: ApiInterface) {

    suspend fun getAddressList(userId: String?) =
        apiInterface.getAddressList(userId, Constant.DEVICE_TYPE)

    suspend fun addAddressRequest(jsonObject: JsonObject) =
        apiInterface.addAddressRequest("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun deleteAddressRequest(jsonObject: JsonObject) =
        apiInterface.deleteAddressRequest("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun updateAddressRequest(jsonObject: JsonObject) =
        apiInterface.updateAddressRequest("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun setAddress(jsonObject: JsonObject) =
        apiInterface.setAddress("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun orderPlaceRequest(userId: String?,jsonObject: JsonObject) =
        apiInterface.orderPlaceRequest(userId, Constant.DEVICE_TYPE, jsonObject)


    suspend fun getOTPRequest(jsonObject: JsonObject) =
        apiInterface.getOTPRequest("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun sendLoginRequest(userId :String,jsonObject: JsonObject) =
        apiInterface.sendLoginRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun sendAutoLoginRequest(userId :String,jsonObject: JsonObject) =
        apiInterface.sendAutoLoginRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun synchDataRequest(userId :String,jsonObject: RequestBody) =
        apiInterface.synchDataRequest(userId, Constant.DEVICE_TYPE, jsonObject)



    suspend fun updateProfileRequest(jsonObject: JsonObject) =
        apiInterface.updateProfileRequest("", Constant.DEVICE_TYPE, jsonObject)

}
package naya.ganj.app.data.mycart.repositry

import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject

class AddressListRespositry(var apiInterface: ApiInterface) {

    suspend fun getAddressList() =
        apiInterface.getAddressList(Constant.USER_ID, Constant.DEVICE_TYPE)

    suspend fun addAddressRequest(jsonObject: JsonObject) =
        apiInterface.addAddressRequest(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)

    suspend fun deleteAddressRequest(jsonObject: JsonObject) =
        apiInterface.deleteAddressRequest(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)

    suspend fun updateAddressRequest(jsonObject: JsonObject) =
        apiInterface.updateAddressRequest(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)

    suspend fun setAddress(jsonObject: JsonObject) =
        apiInterface.setAddress(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)

    suspend fun orderPlaceRequest(jsonObject: JsonObject) =
        apiInterface.orderPlaceRequest(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)


    suspend fun getOTPRequest(jsonObject: JsonObject) =
        apiInterface.getOTPRequest(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)
}
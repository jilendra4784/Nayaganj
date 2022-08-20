package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.utility.NetworkResult
import okhttp3.RequestBody
import org.json.JSONObject

class LoginResponseViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<NetworkResult<LoginResponseModel>>()
    private val apiResponseData = MutableLiveData<NetworkResult<ApiResponseModel>>()

    fun getLoginResponse(userId : String,jsonObject: JsonObject): LiveData<NetworkResult<LoginResponseModel>> {
        viewModelScope.launch {

            try{
                val result=repo.sendLoginRequest(userId,jsonObject)
                if(result.isSuccessful){
                    mutableLiveData.value=NetworkResult.Success(result.body()!!)
                }else{
                    mutableLiveData.value=NetworkResult.Error(result.body()!!.msg)
                }

            }catch (e: Exception){
                mutableLiveData.value=NetworkResult.Error(e.message.toString())
            }


        }
        return  mutableLiveData
    }

    fun getAutoLoginResponse(userId : String,jsonObject: JsonObject): LiveData<NetworkResult<LoginResponseModel>> {
        viewModelScope.launch {
            try{
                val result=repo.sendAutoLoginRequest(userId,jsonObject)
                if(result.isSuccessful){
                    mutableLiveData.value=NetworkResult.Success(result.body()!!)
                }else{
                    mutableLiveData.value=NetworkResult.Error(result.body()!!.msg)
                }

            }catch (e: Exception){
                mutableLiveData.value=NetworkResult.Error(e.message.toString())
            }
        }
        return  mutableLiveData
    }

    fun synchDataRequest(userId: String, jsonObject: RequestBody) : LiveData<NetworkResult<ApiResponseModel>>{
        viewModelScope.launch {

            try{
                val result=repo.synchDataRequest(userId,jsonObject)
                if(result.isSuccessful){
                    apiResponseData.value=NetworkResult.Success(result.body()!!)
                }else{
                    apiResponseData.value=NetworkResult.Error(result.body()!!.msg)
                }

            }catch (e: Exception){
                apiResponseData.value=NetworkResult.Error(e.message.toString())
            }
        }
        return apiResponseData
    }


}
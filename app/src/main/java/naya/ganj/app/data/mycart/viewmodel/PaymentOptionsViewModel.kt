package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.OrderPlacedModel
import naya.ganj.app.data.mycart.model.ValidateTransResponse
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.utility.NetworkResult
import org.json.JSONObject

class PaymentOptionsViewModel(var repo: AddressListRespositry) : ViewModel() {

    private var mutableLiveData = MutableLiveData<NetworkResult<OrderPlacedModel>>()
    private var paytmPurchaseLiveData = MutableLiveData<NetworkResult<JSONObject>>()
    private var validateTransaction = MutableLiveData<NetworkResult<ValidateTransResponse>>()

    fun orderPlaceRequest(
        userId: String?,
        jsonObject: JsonObject
    ): LiveData<NetworkResult<OrderPlacedModel>> {
        viewModelScope.launch {

            try {
                val result = repo.orderPlaceRequest(userId, jsonObject)
                if (result.isSuccessful) {
                    mutableLiveData.value = NetworkResult.Success(result.body()!!)
                } else {
                    mutableLiveData.value = NetworkResult.Error(result.body()!!.msg)
                }
            } catch (e: Exception) {
                mutableLiveData.value = NetworkResult.Error(e.message.toString())
            }

        }
        return mutableLiveData
    }


    fun paytmPurchaseRequest(
        userId: String?,
        jsonObject: JsonObject
    ): LiveData<NetworkResult<JSONObject>> {
        viewModelScope.launch {
            try {
                val result = repo.paytmPurchaseRequest(userId, jsonObject)
                if (result.isSuccessful) {
                    paytmPurchaseLiveData.value = NetworkResult.Success(result.body()!!)
                } else {
                    paytmPurchaseLiveData.value = NetworkResult.Error(result.body()!!.toString())
                }
            } catch (e: Exception) {
                paytmPurchaseLiveData.value = NetworkResult.Error(e.message.toString())
            }

        }
        return paytmPurchaseLiveData
    }

    fun verifyTransactionStatus(
        userId: String?,
        jsonObject: JsonObject
    ): LiveData<NetworkResult<ValidateTransResponse>> {
        viewModelScope.launch {
            try {
                val result = repo.verifyTransactionStatus(userId, jsonObject)
                if (result.isSuccessful) {
                    validateTransaction.value = NetworkResult.Success(result.body()!!)
                } else {
                    validateTransaction.value = NetworkResult.Error(result.body()!!.toString())
                }
            } catch (e: Exception) {
                validateTransaction.value = NetworkResult.Error(e.message.toString())
            }

        }
        return validateTransaction
    }


}
package naya.ganj.app.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddAddressViewModel
import naya.ganj.app.data.mycart.viewmodel.AddressListViewModel
import naya.ganj.app.data.mycart.viewmodel.LoginViewModel
import naya.ganj.app.data.mycart.viewmodel.PaymentOptionsViewModel


class MyViewModelFactory constructor(val repo: AddressListRespositry) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressListViewModel::class.java)) {
            return AddressListViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(AddAddressViewModel::class.java)) {
            return AddAddressViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(PaymentOptionsViewModel::class.java)) {
            return PaymentOptionsViewModel(this.repo) as T
        }else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(this.repo) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }


}
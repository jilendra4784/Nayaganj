package naya.ganj.app.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.*


class MyViewModelFactory constructor(val repo: AddressListRespositry) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressListViewModel::class.java)) {
            return AddressListViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(AddAddressViewModel::class.java)) {
            return AddAddressViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(PaymentOptionsViewModel::class.java)) {
            return PaymentOptionsViewModel(this.repo) as T
        }else if (modelClass.isAssignableFrom(OTPViewModel::class.java)) {
            return OTPViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(LoginResponseViewModel::class.java)) {
            return LoginResponseViewModel(this.repo) as T
        }else if (modelClass.isAssignableFrom(PersonalDetailViewModel::class.java)) {
            return PersonalDetailViewModel(this.repo) as T
        }else {
            throw IllegalArgumentException("ViewModel Not Found")
        }


    }


}
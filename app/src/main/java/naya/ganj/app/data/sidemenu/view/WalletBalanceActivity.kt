package naya.ganj.app.data.sidemenu.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.MyOrderViewModel
import naya.ganj.app.data.sidemenu.viewmodel. MyWalletViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityWalletBalanceBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility

class WalletBalanceActivity : AppCompatActivity() {
    lateinit var binding:ActivityWalletBalanceBinding
    lateinit var viewModel:MyWalletViewModel
    lateinit var app: Nayaganj
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityWalletBalanceBinding.inflate(layoutInflater)
        app=applicationContext as Nayaganj
          viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[MyWalletViewModel::class.java]
        setContentView(binding.root)

        init()
    }

    private fun init(){
        binding.include.ivBackArrow.setOnClickListener{finish()}
        if(app.user.getAppLanguage()==1){
            binding.include.toolbarTitle.text=resources.getString(R.string.my_wallet_h)
            binding.walletText.text = getString(R.string.balance_h)
        }else{
            binding.include.toolbarTitle.text=resources.getString(R.string.my_wallet)
        }

        if(Utility.isAppOnLine(this@WalletBalanceActivity,object:OnInternetCheckListener{
                override fun onInternetAvailable() {
                    getWalletBalance()
                }
            })){
            getWalletBalance()
        }
    }

    private fun getWalletBalance() {
        viewModel.getWalletBalance(app.user.getUserDetails()?.userId).observe(this){ response->
            when(response){
                is NetworkResult.Success->{
                    binding.walletAmount.text=resources.getString(R.string.Rs)+response.data?.walletBalance.toString()
                }
                is NetworkResult.Error ->{
                    Toast.makeText(this@WalletBalanceActivity,response.message,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
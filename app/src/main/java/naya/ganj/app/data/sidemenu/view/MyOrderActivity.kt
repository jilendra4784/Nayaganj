package naya.ganj.app.data.sidemenu.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.adapter.MyOrderListAdapter
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.MyOrderViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityMyOrddrBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Utility

class MyOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrddrBinding
    lateinit var viewModel: MyOrderViewModel
    lateinit var app: Nayaganj
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrddrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[MyOrderViewModel::class.java]

        binding.include4.ivBackArrow.setOnClickListener { finish() }
        if(app.user.getAppLanguage()==1){
            binding.include4.toolbarTitle.text=resources.getString(R.string.myorders_h)
        }else{
            binding.include4.toolbarTitle.text = "My Order List"
        }
    }

    override fun onResume() {
        super.onResume()
        getMyOrderList()
    }

    private fun getMyOrderList() {
        if (app.user.getLoginSession()) {
            viewModel.getMyOrderList(app.user.getUserDetails()!!.userId).observe(this) {
                if(it!=null && it.ordersList.isNotEmpty()){
                    binding.rvMyOrderList.layoutManager = LinearLayoutManager(this@MyOrderActivity)
                    binding.rvMyOrderList.adapter =
                        MyOrderListAdapter(this@MyOrderActivity, it.ordersList)
                    binding.rvMyOrderList.isNestedScrollingEnabled = false
                    Utility.listAnimation(binding.rvMyOrderList)

                    binding.rvMyOrderList.visibility= View.VISIBLE
                  //  binding.tvNodata.visibility= View.GONE
                }else{
                    binding.rvMyOrderList.visibility= View.GONE
                    //binding.tvNodata.visibility= View.VISIBLE

                }

            }
        }
    }
}
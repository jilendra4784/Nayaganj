package naya.ganj.app.data.sidemenu.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.sidemenu.adapter.MyOrderListAdapter
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.MyOrderViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityMyOrddrBinding
import naya.ganj.app.retrofit.RetrofitClient

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
        binding.include4.toolbarTitle.text = "My Order List"
        getMyOrderList()
    }

    private fun getMyOrderList() {
        if (app.user.getLoginSession()) {
            viewModel.getMyOrderList(app.user.getUserDetails()!!.userId).observe(this) {
                binding.rvMyOrderList.layoutManager = LinearLayoutManager(this@MyOrderActivity)
                binding.rvMyOrderList.adapter =
                    MyOrderListAdapter(this@MyOrderActivity, it.ordersList)
                binding.rvMyOrderList.isNestedScrollingEnabled = false
            }
        }
    }
}
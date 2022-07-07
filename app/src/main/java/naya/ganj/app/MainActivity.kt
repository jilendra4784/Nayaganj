package naya.ganj.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import naya.ganj.app.data.home.view.HomeFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.sidemenu.view.MyOrderActivity
import naya.ganj.app.databinding.ActivityMainBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {


    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    var orderID: String? = null
    var notificationsBadge: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.menu.forEach { it.isEnabled = true }
        orderID = intent.getStringExtra("ORDER_ID")
        if (orderID != null) {
            if (orderID.equals("FROM_PRODUCT_DETAIL")) {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().deleteAllProduct(this@MainActivity)
                    withContext(Dispatchers.Main) {
                        getMyCartData("")
                    }
                }
                val action = HomeFragmentDirections.actionMainToMycart("")
                findNavController(R.id.nav_host_fragment_activity_main).navigate(action)
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().deleteAllProduct(this@MainActivity)
                    withContext(Dispatchers.Main) {
                        getMyCartData(orderID!!)
                    }
                }
                val action = HomeFragmentDirections.actionMainToMycart(orderID!!)
                findNavController(R.id.nav_host_fragment_activity_main).navigate(action)
            }

        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDataBase.getInstance(this@MainActivity).productDao().deleteAllProduct()
                withContext(Dispatchers.Main) {
                    getMyCartData("")
                }
            }
        }

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerlayout,
            binding.materialToolbar,
            R.string.open_string,
            R.string.close_string
        )

        toggle.syncState()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

        binding.navView.setOnItemSelectedListener {
            binding.appBarLayout.visibility = View.VISIBLE
            binding.navView.visibility = View.VISIBLE
            when (it.itemId) {
                R.id.navigation_home -> {
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_home)
                }
                R.id.navigation_mycart -> {
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_mycart)
                    setBadgeCount()
                }
                R.id.navigation_dashboard -> {
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_dashboard)
                    setBadgeCount()
                }
                else -> {
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_notifications)
                    setBadgeCount()
                }
            }
            return@setOnItemSelectedListener true
        }
        binding.sideNavigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    showMessage(item.title.toString())
                }
                R.id.myaccount -> {
                    showMessage(item.title.toString())
                }

                R.id.shop_category -> {
                    showMessage(item.title.toString())
                }

                R.id.my_order -> {
                    startActivity(Intent(this@MainActivity, MyOrderActivity::class.java))
                    checkDrawerIsOpen()
                }
                R.id.myvirtualorder -> {
                    showMessage(item.title.toString())
                }
                R.id.all_offer -> {
                    showMessage(item.title.toString())
                }
                R.id.share_App -> {
                    showMessage(item.title.toString())
                }
                R.id.refer_earn -> {
                    showMessage(item.title.toString())
                }
                R.id.customer_support -> {
                    showMessage(item.title.toString())
                }
                R.id.about_us -> {
                    showMessage(item.title.toString())
                }
                R.id.privacy_policy -> {
                    showMessage(item.title.toString())
                }
                R.id.logout -> {
                    showMessage(item.title.toString())
                }

            }
            true
        }

        // Synch MyCart Data with Local Database

    }

    private fun setBadgeCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<ProductDetail> = Utility().getAllProductList(this@MainActivity)
            if (!list.isEmpty()) {
                withContext(Dispatchers.Main) {
                    addBadge(list.size.toString())
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkDrawerIsOpen() {
        if (binding.drawerlayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerlayout.closeDrawer(Gravity.LEFT); //CLOSE Nav Drawer!
        } else {
            binding.drawerlayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
        }
    }

    private fun getMyCartData(orderID: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.ORDER_ID, orderID)

        RetrofitClient.instance.getMyCartData(Constant.USER_ID, Constant.DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<MyCartModel> {
                override fun onResponse(call: Call<MyCartModel>, response: Response<MyCartModel>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            Log.e("TAG", "onResponse: " + response.body()!!.cartList.size)
                            for (item in response.body()!!.cartList) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    Utility().insertProduct(
                                        this@MainActivity,
                                        item.productId,
                                        item.variantId,
                                        item.actualPrice.toDouble()
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<MyCartModel>, t: Throwable) {

                }
            })
    }


    private fun addBadge(count: String) {

        val mbottomNavigationMenuView = binding.navView.getChildAt(0) as BottomNavigationMenuView
        notificationsBadge = LayoutInflater.from(this).inflate(
            R.layout.custom_badge_layout,
            mbottomNavigationMenuView, false
        )
        val textView = notificationsBadge?.findViewById<TextView>(R.id.badge_count)
        if (textView != null) {
            textView.text = count
        }
        binding.navView.addView(notificationsBadge)
    }
}
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.data.home.view.HomeFragmentDirections
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.data.sidemenu.view.MyOrderActivity
import naya.ganj.app.databinding.ActivityMainBinding
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Utility


class MainActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    var orderID: String? = null
    var notificationsBadge: View? = null
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        //binding.navView.menu.forEach { it.isEnabled = true }
        orderID = intent.getStringExtra("ORDER_ID")
        if (orderID != null) {
            val action = HomeFragmentDirections.actionMainToMycart(orderID!!)
            findNavController(R.id.nav_host_fragment_activity_main).navigate(action)
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            showAppBar()
            showBottomNav()
            when (destination.id) {
                R.id.navigation_home -> {}
                R.id.navigation_dashboard -> {}
                R.id.navigation_notifications -> {}
                R.id.navigation_mycart -> {
                    Log.e("TAG", "onCreate: ")

                    startActivity(Intent(this@MainActivity, MyCartActivity::class.java))
                }
                else -> {
                    //hideBottomNav()
                    // hideBottomNav()
                }
            }
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
                    checkDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, MyOrderActivity::class.java))
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
                    showLogoutDialog()
                }

            }
            true
        }

        // Todo Hide Side Menu Item
        binding.sideNavigation.menu.findItem(R.id.logout).isVisible = app.user.getLoginSession()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("LOGOUT")
            .setMessage("Are you sure, you want to logout?")
            .setPositiveButton(
                "YES"
            ) { dialogInterface, i ->
                checkDrawerIsOpen()
                app.user.clearSharedPreference()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialogInterface.dismiss()
                finish()
            }
            .setNegativeButton(
                "CANCEL"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
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

    private fun showBottomNav() {
        binding.navView.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        binding.navView.visibility = View.GONE
    }

    private fun showAppBar() {
        binding.appBarLayout.visibility = View.VISIBLE
    }

    private fun hideAppBar() {
        binding.appBarLayout.visibility = View.GONE
    }

}
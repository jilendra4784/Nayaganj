package naya.ganj.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.data.mycart.view.LoginActivity
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.data.sidemenu.view.*
import naya.ganj.app.databinding.ActivityMainBinding
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Utility


class MainActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    var orderID: String? = null
    var notificationsBadge: View? = null
    lateinit var navController: NavController
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerlayout,
            binding.materialToolbar,
            R.string.open_string,
            R.string.close_string
        )

        toggle.syncState()

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {}
                R.id.navigation_dashboard -> {}
                R.id.navigation_notifications -> {}
                R.id.navigation_mycart -> {
                    val intent = Intent(this@MainActivity, MyCartActivity::class.java)
                    intent.putExtra("ORDER_ID", orderID)
                    startActivity(intent)
                }
            }
        }

        binding.sideNavigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    isDrawerIsOpen()
                    moveToHomeFragment()

                }
                R.id.myaccount -> {
                    startActivity(Intent(this@MainActivity,MyAccountActivity::class.java))
                }

                R.id.shop_category -> {
                    isDrawerIsOpen()
                    moveToDashboard()

                }

                R.id.my_order -> {
                    isDrawerIsOpen()
                    startActivity(Intent(this@MainActivity, MyOrderActivity::class.java))
                }
                R.id.myvirtualorder -> {
                    showMessage(item.title.toString())
                }
                R.id.all_offer -> {
                    showMessage(item.title.toString())
                }
                R.id.share_App -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                }
                R.id.refer_earn -> {
                    showMessage(item.title.toString())
                }
                R.id.customer_support -> {
                    startActivity(Intent(this@MainActivity, CustomerSupportActivity::class.java))
                }
                R.id.about_us -> {
                    startActivity(Intent(this@MainActivity, AboutUsActivity::class.java))
                }
                R.id.privacy_policy -> {
                    startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                }
                R.id.logout -> {
                    showLogoutDialog()
                }

            }
            true
        }

        // Todo Hide Side Menu Item
        binding.sideNavigation.menu.findItem(R.id.myaccount).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.my_order).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.myvirtualorder).isVisible =
            app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.refer_earn).isVisible = app.user.getLoginSession()
        binding.sideNavigation.menu.findItem(R.id.logout).isVisible = app.user.getLoginSession()


        val userName =
            binding.sideNavigation.getHeaderView(0).findViewById(R.id.tv_user_name) as TextView
        val mobileNo =
            binding.sideNavigation.getHeaderView(0).findViewById(R.id.tv_mobile) as TextView
        val llLoginSignUp = binding.sideNavigation.getHeaderView(0)
            .findViewById(R.id.ll_login_signup_layout) as LinearLayout
        val llUserInfoLayout = binding.sideNavigation.getHeaderView(0)
            .findViewById(R.id.ll_user_info_layout) as LinearLayout


        if (app.user.getLoginSession()) {
            if (app.user.getUserDetails()?.name.equals("")) {
                userName.text = resources.getString(R.string.user_name)
            } else {
                userName.text = app.user.getUserDetails()?.name
            }
            mobileNo.text = app.user.getUserDetails()?.mNumber
            llUserInfoLayout.visibility = View.VISIBLE
        } else {
            llLoginSignUp.visibility = View.VISIBLE
        }

        binding.sideNavigation.getHeaderView(0).setOnClickListener {
            if (!app.user.getLoginSession()) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        LoginActivity::class.java
                    )
                )
            }
        }
    }

    private fun moveToDashboard() {
        if (navController.currentDestination?.id != R.id.navigation_dashboard) {
            if (navController.currentDestination?.id == R.id.navigation_home) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_home_to_navigation_dashboard)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_dashboard)
            }
        }
    }

    private fun moveToHomeFragment() {
        if (navController.currentDestination?.id != R.id.navigation_home) {
            if (navController.currentDestination?.id == R.id.navigation_notifications) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_notifications_to_navigation_home)
            } else {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_dashboard_to_navigation_home)
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("LOGOUT")
            .setMessage("Are you sure, you want to logout?")
            .setPositiveButton(
                "YES"
            ) { dialogInterface, i ->
                isDrawerIsOpen()
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

    private fun isDrawerIsOpen() {
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

}
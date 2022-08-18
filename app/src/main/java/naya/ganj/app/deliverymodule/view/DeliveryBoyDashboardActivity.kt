package naya.ganj.app.deliverymodule.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityDeliveryBoyDashboardBinding

class DeliveryBoyDashboardActivity : AppCompatActivity() {

    lateinit var binding:ActivityDeliveryBoyDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityDeliveryBoyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = Navigation.findNavController(this, R.id.delivery_host_fragment)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

    }

    override fun onResume() {
        super.onResume()
        binding.include15.ivBackArrow.visibility= View.GONE
        binding.include15.tvToolbarTitle.text="Welcome To NayaGanj"
    }
}
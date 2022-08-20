package naya.ganj.app.deliverymodule.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.databinding.FragmentProfileBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.utility.Utility

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        app = requireActivity().applicationContext as Nayaganj
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUsername.text = app.user.getUserDetails()?.name
        binding.tvMobile.text = app.user.getUserDetails()?.mNumber

        binding.tvLogout.setOnClickListener {
            if (Utility.isAppOnLine(requireActivity(),object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        showLogoutDialog()
                    }
                }))
                showLogoutDialog()
        }
    }


    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("LOGOUT")
            .setMessage("Are you sure, you want to logout?")
            .setPositiveButton(
                "YES"
            ) { dialogInterface, i ->
                dialogInterface.dismiss()
                app.user.clearSharedPreference()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(
                "CANCEL"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }


}
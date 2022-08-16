package naya.ganj.app.deliverymodule.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.FragmentOrdersBinding
import naya.ganj.app.deliverymodule.adapter.DeliverOrdersAdapter
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class OrdersFragment : Fragment() {

    lateinit var binding: FragmentOrdersBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            requireActivity(),
            DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]
        app = requireActivity().applicationContext as Nayaganj
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDeliveryOrder.setOnClickListener {
            binding.btnDeliveryOrder.background.setTint(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.purple_500
                )
            )
            binding.btnDeliveryOrder.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.btnReturnOrder.background.setTint(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.btnReturnOrder.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.red_color
                )
            )
            //getDeliveryOrdersData()

        }
        binding.btnReturnOrder.setOnClickListener {
            binding.btnReturnOrder.background.setTint(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.purple_500
                )
            )
            binding.btnReturnOrder.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.btnDeliveryOrder.background.setTint(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.btnDeliveryOrder.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.red_color
                )
            )

            //getOrderList(app.toString())
        }

        // Delivery Orders List
        binding.rvDeliveryOrdersList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvDeliveryOrdersList.isNestedScrollingEnabled = false

        getOrderList(app.user.getUserDetails()?.userId, "")

    }


    private fun getOrderList(userId: String?, orderStatus: String) {

        if (Utility().isAppOnLine(requireActivity())) {
            val jsonObject = JsonObject()
            if (orderStatus.equals("")) {
                jsonObject.addProperty(Constant.DeliveryOrderStatus, orderStatus);
            } else {
                jsonObject.addProperty(Constant.DeliveryOrderStatus, Constant.RETURNVERIFIED);
            }

            viewModel.getDeliveryOrdersData(userId, jsonObject)
                .observe(requireActivity()) {
                    if (it != null) {
                        if (it.ordersList.isNotEmpty()) {
                            binding.rvDeliveryOrdersList.visibility = View.VISIBLE
                            binding.rvDeliveryOrdersList.adapter =
                                DeliverOrdersAdapter(requireActivity(), it.ordersList)
                        }
                    }
                }
        }
    }
}
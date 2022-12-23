package naya.ganj.app.deliverymodule.view

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.FragmentOrdersBinding
import naya.ganj.app.deliverymodule.adapter.OrdersAdapter
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersFragment : Fragment(), OrdersAdapter.chnageOrderStatus {

    lateinit var binding: FragmentOrdersBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj
    var orderType = "delivery"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

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
            orderType = "delivery"

            binding.rvDeliveryOrdersList.visibility = View.GONE
            binding.rvReturnOrdersList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
            Handler(Looper.getMainLooper()).postDelayed({ getOrderList(app.user.getUserDetails()?.userId, "") }, 300)

        }
        binding.btnReturnOrder.setOnClickListener {
            orderType = "return"
            binding.rvReturnOrdersList.visibility = View.GONE
            binding.rvDeliveryOrdersList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
            Handler(Looper.getMainLooper()).postDelayed({ getOrderList(app.user.getUserDetails()?.userId, Constant.RETURNVERIFIED) }, 300)

        }

        // Delivery Orders List
        binding.rvDeliveryOrdersList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvDeliveryOrdersList.isNestedScrollingEnabled = false

        binding.rvReturnOrdersList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvReturnOrdersList.isNestedScrollingEnabled = false

        binding.progressBar.visibility = View.VISIBLE

        getOrderList(app.user.getUserDetails()?.userId, "")

    }


    private fun getOrderList(userId: String?, orderStatus: String) {
        if(Utility.isAppOnLine(requireActivity(),object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getOrderList(app.user.getUserDetails()?.userId, "")
                }
            })){
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.DeliveryOrderStatus, orderStatus);

            RetrofitClient.instance.getDeliveryOrders(userId, Constant.DEVICE_TYPE, jsonObject)
                .enqueue(object : Callback<DeliveryOrdersModel> {
                    override fun onResponse(
                        call: Call<DeliveryOrdersModel>,
                        response: Response<DeliveryOrdersModel>
                    ) {
                        binding.progressBar.visibility = View.GONE
                        binding.llEmptyStockLayout.visibility=View.GONE
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (orderStatus == "") {
                                if (result != null && result.ordersList.isNotEmpty()) {
                                    binding.rvDeliveryOrdersList.visibility = View.VISIBLE
                                    binding.rvReturnOrdersList.visibility = View.GONE
                                    binding.rvDeliveryOrdersList.adapter = OrdersAdapter(
                                        requireActivity(),
                                        result.ordersList,
                                        orderStatus,
                                        orderType,
                                        this@OrdersFragment
                                    )
                                }else{
                                    binding.llEmptyStockLayout.visibility=View.VISIBLE
                                    binding.rvDeliveryOrdersList.visibility = View.GONE
                                }
                            } else {
                                if (result != null && result.ordersList.isNotEmpty()) {
                                    binding.rvReturnOrdersList.visibility = View.VISIBLE
                                    binding.rvDeliveryOrdersList.visibility = View.GONE
                                    binding.rvReturnOrdersList.adapter = OrdersAdapter(
                                        requireActivity(),
                                        result.ordersList,
                                        orderStatus,
                                        orderType,
                                        this@OrdersFragment
                                    )
                                }else{
                                    binding.llEmptyStockLayout.visibility=View.VISIBLE
                                    binding.rvDeliveryOrdersList.visibility = View.GONE
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DeliveryOrdersModel>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        Utility.serverNotResponding(requireActivity(),"")
                    }
                })
        }
        }


    override fun orderIdOrderStatus(
        orderId: String,
        orderStatus: String,
        orderStatusTextView: TextView,
        paymentMode: String
    ) {
        if (paymentMode.equals("COD", ignoreCase = true) && orderStatus == Constant.DELIVERED) {
            deliverProductDialog(orderId, paymentMode)
        } else if (orderStatus == Constant.DISPATCHED || orderStatus == Constant.COLLECTED) {
            orderStatusDialog(orderId, orderStatus, orderStatusTextView)
        } else {
            orderStatusDialog(orderId, orderStatus, orderStatusTextView)
        }
    }

    private fun deliverProductDialog(orderId: String, paymentMode: String) {
        Log.e("TAG", "deliverProductDialog: "+"" )
        var paymentModeStatus = ""
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.deliver_order_payment_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val ivClose = dialog.findViewById(R.id.iv_close) as ImageView
        val edtAmount = dialog.findViewById(R.id.edt_amount) as EditText
        val radioCash = dialog.findViewById(R.id.radioButton_cash) as RadioButton
        val radioPaytm = dialog.findViewById(R.id.radio_button_paytm) as RadioButton
        val btnSubmit = dialog.findViewById(R.id.btn_submit) as Button

        ivClose.setOnClickListener { dialog.dismiss() }


        radioCash.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                radioCash.isChecked = true
                radioPaytm.isChecked = false
                paymentModeStatus = "CASH"
            }
        }

        radioPaytm.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                radioCash.isChecked = false
                radioPaytm.isChecked = true
                paymentModeStatus = "PAYTM"
            }
        }

        btnSubmit.setOnClickListener {
            val amount = edtAmount.text.toString()
            if (amount == "") {
                Utility().showToast(requireActivity(), "Please Enter the Amount.")
            } else if (paymentModeStatus == "") {
                Utility().showToast(requireActivity(), "Please Select Payment Mode")
            } else {
                dialog.dismiss()

                if(Utility.isAppOnLine(requireActivity(),object : OnInternetCheckListener {
                        override fun onInternetAvailable() {
                            deliveryOrderPaymentApi(orderId, amount, paymentModeStatus)
                        }
                    }))
                deliveryOrderPaymentApi(orderId, amount, paymentModeStatus)
            }
        }

        dialog.show()
    }

    private fun deliveryOrderPaymentApi(
        orderId: String,
        amount: String,
        paymentModeStatus: String
    ) {

            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.paymentMode, paymentModeStatus)
            jsonObject.addProperty(Constant.amount, amount)

            viewModel.deliverOrderPaymentRequest(app.user.getUserDetails()?.userId, jsonObject)
                .observe(requireActivity()) {
                    if (it.status) {
                        getOrderList(app.user.getUserDetails()?.userId, "")
                        Toast.makeText(
                            activity?.applicationContext!!,
                            "You have delivered product successfully to the customer.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            activity?.applicationContext!!,
                            it.msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


    private fun orderStatusDialog(
        orderId: String,
        orderStatus: String,
        orderStatusTextView: TextView
    ) {
        var alertDialog: AlertDialog?=null
        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.my_order))

        if (orderStatus.equals(Constant.COLLECTED, ignoreCase = true)) {
            builder.setTitle(getString(R.string.collect_order))
            builder.setMessage("Are you sure you want to Collect this order?")
        } else if (orderStatus.equals(Constant.DISPATCHED, ignoreCase = true)) {
            builder.setTitle(getString(R.string.dispatch_order))
            builder.setMessage("Are you sure you want to Dispatch this order?")
        } else if (orderStatus.equals(Constant.DELIVERED, ignoreCase = true)) {
            builder.setTitle(getString(R.string.deliver_order))
            builder.setMessage("Have you reached Customer location & Ready to Deliver Product.")
        } else if (orderStatus.equals(Constant.RETURNSUCCESS, ignoreCase = true)) {
            builder.setTitle(getString(R.string.return_products))
            builder.setMessage("Have you collected product from the customer & ready to return product.")
        }
        builder.setPositiveButton("Yes")
        { _, _ ->
            alertDialog?.dismiss()
                ChangeStatusApi(orderId, orderStatus, orderStatusTextView)
        }
        builder.setNegativeButton("No") { dialogInterface, which -> }
        alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(R.color.gray))
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.dark_gray))

    }

    private fun ChangeStatusApi(
        orderId: String,
        orderStatus: String,
        orderStatusTextView: TextView
    ) {

        orderStatusTextView.isEnabled=false
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.orderId, orderId)
        jsonObject.addProperty(Constant.orderStatus, orderStatus)
        viewModel.changeOrderStatusAPIRequest(app.user.getUserDetails()?.userId,jsonObject).observe(requireActivity()) {

            if (it.status) {

                if (orderStatus.equals(Constant.COLLECTED, ignoreCase = true))
                {
                    Toast.makeText(activity?.applicationContext!!, "You have collected order successfully", Toast.LENGTH_SHORT).show()
                }
                else if (orderStatus.equals(Constant.DISPATCHED, ignoreCase = true))
                {
                    Toast.makeText(activity?.applicationContext!!, "You have dispatched order successfully", Toast.LENGTH_SHORT).show()
                }

                else if (orderStatus.equals(Constant.RETURNSUCCESS, ignoreCase = true))
                {

                    Toast.makeText(activity?.applicationContext!!, "You have returned product successfully", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    //getLastLocation()
                    Toast.makeText(activity?.applicationContext!!, "You have delivered order successfully to the customer", Toast.LENGTH_SHORT).show()
                }
                orderStatusTextView.isEnabled = true
                if(orderStatus.equals(Constant.RETURNSUCCESS,ignoreCase = true))
                {
                    getOrderList(app.user.getUserDetails()?.userId,"RETURNVERIFIED")
                }
                else
                {
                    getOrderList(app.user.getUserDetails()?.userId,"")
                }
            }
            else
            {
                Toast.makeText(activity?.applicationContext!!, it.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
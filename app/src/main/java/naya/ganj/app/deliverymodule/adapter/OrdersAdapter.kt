package naya.ganj.app.deliverymodule.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.databinding.AdapterDeliveryOrdersLayoutBinding
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel
import naya.ganj.app.deliverymodule.view.OrderDetailActivity
import naya.ganj.app.utility.Constant

class OrdersAdapter(
    val context: Context,
    private val orderList: List<DeliveryOrdersModel.Orders>,
    private val orderType: String,
    val type: String,
    val changeStatusCallback:chnageOrderStatus
) :
    RecyclerView.Adapter<OrdersAdapter.MyViewHolder>() {


    interface chnageOrderStatus {
        fun orderIdOrderStatus(orderId: String, orderStatus:String, orderStatusTextView: TextView, paymentMode: String)
    }


    class MyViewHolder(val binding: AdapterDeliveryOrdersLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            AdapterDeliveryOrdersLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var orderStatus:List<String>?=null
        val order: DeliveryOrdersModel.Orders = orderList[position]
        holder.binding.tvOrderId.text = order.orderId
        holder.binding.tvItems.text = order.totalItems.toString() + " " + "Items"
        holder.binding.tvAmount.text =
            context.resources.getString(R.string.Rs) + order.totalAmount.toString()
        holder.binding.tvOrderOn.text = order.date
        holder.binding.tvPaymentStatus.text = order.paymentStatus
        holder.binding.tvAddressType.text = order.address.nickName
        holder.binding.tvDeliveryAddress.text =
            order.address.houseNo + order.address.landmark + order.address.city + order.address.pincode

        if (orderType == "") {
            orderStatus = order.buttonIndex.split("|")[1].split(",")
            holder.binding.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[2]))
            holder.binding.tvOrderStatus.text = orderStatus[1]
            holder.binding.cvStatusButtonCardview.visibility = View.VISIBLE
        } else {
            holder.binding.cvStatusButtonCardview.visibility = View.GONE
        }

        holder.binding.tvOrderStatus.setOnClickListener{
            changeStatusCallback.orderIdOrderStatus(order.orderId, orderStatus?.get(0) ?: "",holder.binding.tvOrderStatus,order.paymentMode)
        }

        // For Delivery

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra(Constant.ORDER_ID, order.orderId)
            intent.putExtra(Constant.Type, type)
            intent.putExtra(Constant.FragmetType, "ORDERS")
            context.startActivity(intent)
        }
    }
}
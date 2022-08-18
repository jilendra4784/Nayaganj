package naya.ganj.app.deliverymodule.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.databinding.AdapterDeliveredOrdersLayoutBinding
import naya.ganj.app.deliverymodule.model.DeliveredOrdersModel
import naya.ganj.app.deliverymodule.view.OrderDetailActivity
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class OrdersHistoryAdapter(
    val context: Context,
    val type: String,
    val list: List<DeliveredOrdersModel.Orders>
) :
    RecyclerView.Adapter<OrdersHistoryAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            AdapterDeliveredOrdersLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    class MyViewHolder(val binding: AdapterDeliveredOrdersLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val orders: DeliveredOrdersModel.Orders = list[position]
        holder.binding.tvOrderId.text = orders.orderId
        holder.binding.tvItems.text = orders.totalItems.toString() + " Items"
        holder.binding.tvOrderOn.text = orders.date
        holder.binding.tvAddress.text =
            orders.address.houseNo + orders.address.landmark + orders.address.city + "-" + orders.address.pincode

        holder.binding.tvOrderStatus.text = orders.orderStatus
        holder.binding.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
        holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor("#20BA44"))

        holder.binding.tvAmount.text =
            context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(orders.totalAmount)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra(Constant.ORDER_ID, orders.orderId)
            intent.putExtra(Constant.Type, type)
            intent.putExtra(Constant.FragmetType, "HISTORY")
            context.startActivity(intent)
        }
    }

}
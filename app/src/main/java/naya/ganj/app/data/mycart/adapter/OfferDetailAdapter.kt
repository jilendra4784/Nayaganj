package naya.ganj.app.data.mycart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.data.mycart.adapter.OfferDetailAdapter.MyViewHolder
import naya.ganj.app.databinding.AdapterOfferDetailLayoutBinding

class OfferDetailAdapter(val details: List<String>) : RecyclerView.Adapter<MyViewHolder>() {
    class MyViewHolder(val binding: AdapterOfferDetailLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            AdapterOfferDetailLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.name.text = details[position]
    }

    override fun getItemCount(): Int {
        return details.size
    }

}
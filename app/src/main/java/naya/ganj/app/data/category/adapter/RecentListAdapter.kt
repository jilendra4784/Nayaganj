package naya.ganj.app.data.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.databinding.RecentAdapterLayoutBinding
import naya.ganj.app.roomdb.entity.RecentSuggestion

class RecentListAdapter(val list: List<RecentSuggestion>) :
    RecyclerView.Adapter<RecentListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: RecentAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            RecentAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
             holder.binding.tvRecentTitle.text= list.get(position).query
    }


}
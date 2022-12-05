package naya.ganj.app.data.home.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.databinding.AdapterHomeLayoutBinding
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class HomeAdapter(
    val context: Context,
    val category: List<HomePageModel.Data.Category>,
    val app: Nayaganj,
    val cateImages: Array<Int>
) :
    RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: AdapterHomeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            AdapterHomeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvTitle.text = Utility.convertLanguage(category[position].category, app)
        holder.binding.imageView11.setBackgroundResource(cateImages[position])
        holder.binding.materialCardView.setOnClickListener {
            val intent = Intent(context, ProductListActivity::class.java)
            intent.putExtra(Constant.CATEGORY_ID, category[position].id)
            intent.putExtra(Constant.CATEGORY_NAME, category[position].category)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return category.size
    }
}
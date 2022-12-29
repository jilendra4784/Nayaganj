package naya.ganj.app.data.home.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.databinding.AdapterHomeLayoutBinding
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.ImageManager
import naya.ganj.app.utility.Utility

class HomeAdapter(
    val context: Context,
    val category: List<HomePageModel.Data.Category>,
    val app: Nayaganj
) :
    RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: AdapterHomeLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(AdapterHomeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvTitle.text = Utility.convertLanguage(category[position].category, app)
        if(category[position].imgUrl.isNotEmpty()){
            /*val imgURL=app.user.getUserDetails()?.configObj?.productImgUrl+category[position].imgUrl[0]
            Glide.with(context).load(imgURL).error(R.drawable.default_image).into(holder.binding.ivCatImage)*/

            ImageManager.onLoadingImage(app,context, category[position].imgUrl[0],holder.binding.ivCatImage)

        }
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
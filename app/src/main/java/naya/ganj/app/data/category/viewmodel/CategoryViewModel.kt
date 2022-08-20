package naya.ganj.app.data.category.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.data.category.repositry.CategoryRepositry

class CategoryViewModel : ViewModel() {

    private val categoryRepositry = CategoryRepositry()

    fun getCategoryData(context: Context): LiveData<CategoryDataModel> {
        return categoryRepositry.getCategoryData(context)
    }

}
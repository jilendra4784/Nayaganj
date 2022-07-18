package naya.ganj.app.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.PREFS_NAME

class User(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserDetail(userDetails: LoginResponseModel.UserDetails) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(userDetails)
        editor.putString(Constant.USER_DETAILS, json)
        editor.apply()

    }

    fun getUserDetails(): LoginResponseModel.UserDetails? {
        val gson = Gson()
        val json: String? = sharedPref.getString(Constant.USER_DETAILS, null)
        return if (json == null) {
            null
        } else {
            gson.fromJson(json, LoginResponseModel.UserDetails::class.java)
        }

    }

    fun setLoginSession(loginSession: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(Constant.LOGIN_SESSION, loginSession)
        editor.apply()
    }

    fun getLoginSession(): Boolean {
        return sharedPref.getBoolean(Constant.LOGIN_SESSION, false)
    }

    fun setAppLanguage(language: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(Constant.LANGUAGE, language)
        editor.apply()
    }

    fun getAppLanguage(): Int {
        return sharedPref.getInt(Constant.LANGUAGE, 0)
    }

    fun clearSharedPreference() {
        sharedPref.apply {
            edit()
                .clear()
                .apply()
        }
    }

}

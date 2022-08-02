package naya.ganj.app

import android.app.Application
import com.google.android.material.color.DynamicColors
import naya.ganj.app.sharedpreference.User

class Nayaganj : Application() {

    lateinit var user: User
    override fun onCreate() {
        super.onCreate()
        user = User(applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
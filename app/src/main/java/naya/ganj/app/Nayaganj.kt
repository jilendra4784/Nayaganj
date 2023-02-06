package naya.ganj.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import naya.ganj.app.sharedpreference.User

class Nayaganj : Application() {

    lateinit var user: User
    override fun onCreate() {
        super.onCreate()
        user = User(applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(this)
        FirebaseApp.initializeApp(this)
    }
}
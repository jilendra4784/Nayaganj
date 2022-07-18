package naya.ganj.app

import android.app.Application
import naya.ganj.app.sharedpreference.User

class Nayaganj : Application() {

    lateinit var user: User
    override fun onCreate() {
        super.onCreate()
        user = User(applicationContext)
    }
}
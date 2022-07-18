package naya.ganj.app.retrofit


import com.google.gson.GsonBuilder
import naya.ganj.app.retrofit.URLConstant.Base_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Modifier

object RetrofitClient {

    var mHttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    var mOkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(mHttpLoggingInterceptor)
        .build()

    val instance: ApiInterface by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(Base_URL)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder().excludeFieldsWithModifiers(
                Modifier.TRANSIENT)
                .disableHtmlEscaping().create()))
        .client(mOkHttpClient)
        .build()

        retrofit.create(ApiInterface::class.java)
    }
}
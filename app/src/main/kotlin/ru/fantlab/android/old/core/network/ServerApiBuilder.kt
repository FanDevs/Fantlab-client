package ru.fantlab.android.old.core.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.fantlab.android.old.core.Const
import ru.fantlab.android.old.search.SearchResult
import ru.fantlab.android.old.search.SearchResultDeserializer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

internal object ServerApiBuilder {

	private val HTTP_LOG_TAG = "OkHttp"

	fun createApi(): IServerApi {
		val builder = OkHttpClient.Builder()

		builder.addInterceptor(HeaderInterceptor())
		builder.addInterceptor(StethoInterceptor())

		val loggingInterceptor = HttpLoggingInterceptor { message -> Timber.tag(HTTP_LOG_TAG).d(message) }
		loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
		builder.addInterceptor(loggingInterceptor)

		builder.followRedirects(false)

		val httpClient = builder.build()

		val retrofitBuilder = Retrofit.Builder()
				.baseUrl(Const.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create(
						GsonBuilder()
								.registerTypeAdapter(SearchResult::class.java, SearchResultDeserializer())
								.create()
				))
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.client(httpClient)

		return retrofitBuilder.build().create(IServerApi::class.java)
	}
}
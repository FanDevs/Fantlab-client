package ru.fantlab.android.old.core.network

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NetworkModule {

	@Provides
	@Singleton
	fun provideServerApi() = ServerApiBuilder.createApi()
}
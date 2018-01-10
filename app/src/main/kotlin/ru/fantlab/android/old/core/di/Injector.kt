package ru.fantlab.android.old.core.di

object Injector {

	private var appComponent: AppComponent? = null

	fun init(appComponent: AppComponent) {
		Injector.appComponent = appComponent
	}

	fun getAppComponent(): AppComponent =
			appComponent ?: throw RuntimeException("AppComponent not initialized yet!")
}
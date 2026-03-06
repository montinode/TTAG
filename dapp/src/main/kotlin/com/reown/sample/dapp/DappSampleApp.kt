package com.reown.sample.dapp

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import timber.log.Timber

class DappSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Dapp",
            description = "Kotlin sample dApp using WalletConnect AppKit",
            url = "https://appkit-lab.reown.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-dapp-wc://request"
        )

        val projectId = BuildConfig.PROJECT_ID

        CoreClient.initialize(
            application = this,
            projectId = projectId,
            metaData = appMetaData,
            onError = { error ->
                Timber.e("CoreClient init error: ${error.throwable.message}")
            }
        )

        AppKit.initialize(
            init = Modal.Params.Init(core = CoreClient),
            onError = { error ->
                Timber.e("AppKit init error: ${error.throwable.message}")
            }
        )
    }
}

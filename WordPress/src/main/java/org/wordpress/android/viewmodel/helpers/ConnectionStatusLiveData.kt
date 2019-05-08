package org.wordpress.android.viewmodel.helpers

import android.arch.lifecycle.LiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import org.wordpress.android.viewmodel.helpers.ConnectionStatus.AVAILABLE
import org.wordpress.android.viewmodel.helpers.ConnectionStatus.UNAVAILABLE

enum class ConnectionStatus {
    AVAILABLE,
    UNAVAILABLE
}

/**
 * A LiveData instance that can be injected to keep track of the network availability.
 *
 * This only emits if the network availability changes and not when the user switches between cellular and wi-fi.
 *
 * IMPORTANT: It needs to be observed for the changes to be posted.
 */
class ConnectionStatusLiveData(private val context: Context) : LiveData<ConnectionStatus>() {
    override fun onActive() {
        super.onActive()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, intentFilter)
    }

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(networkReceiver)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val networkInfo = connectivityManager?.activeNetworkInfo

            val nextValue: ConnectionStatus = if (networkInfo?.isConnected == true) AVAILABLE else UNAVAILABLE
            if (value != nextValue) {
                postValue(nextValue)
            }
        }
    }
}

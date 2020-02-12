package com.android.internetradio.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Receiver to Receive Network Change states and re
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    protected Set<NetworkStateReceiverListener> mListeners;

    public NetworkStateReceiver() {
        mListeners = new HashSet<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (null != networkInfo && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            notifyStateToAll(true);
        } else {
            notifyStateToAll(false);
        }
    }

    private void notifyStateToAll(boolean isConnected) {
        for (NetworkStateReceiverListener listener : mListeners)
            notifyState(listener, isConnected);
    }

    private void notifyState(NetworkStateReceiverListener listener, boolean isConnected) {
        if (isConnected)
            listener.onNetworkAvailable();
        else
            listener.onNetworkUnavailable();
    }

    public void addListener(NetworkStateReceiverListener l) {
        mListeners.add(l);
        notifyState(l, false);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        mListeners.remove(l);
    }

    public interface NetworkStateReceiverListener {
        void onNetworkAvailable();

        void onNetworkUnavailable();
    }
}

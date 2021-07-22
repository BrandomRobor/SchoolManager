package me.brandom.schoolmanager.utils

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/*
    This is not my code. Credit goes to the Muzei app:
    https://github.com/muzei/muzei/blob/main/extensions/src/main/java/com/google/android/apps/muzei/util/BroadcastReceiverExt.kt
 */

abstract class BroadcastReceiverExt : BroadcastReceiver() {
    fun goAsync(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        val result = goAsync()

        coroutineScope.launch {
            try {
                block()
            } finally {
                result.finish()
            }
        }
    }
}
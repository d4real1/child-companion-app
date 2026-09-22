package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.service.CompanionService

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
      intent.action == "android.intent.action.QUICKBOOT_POWERON"
    ) {
      Log.d(TAG, "Boot completed event received. Launching background companion service.")
      val serviceIntent = Intent(context, CompanionService::class.java).apply {
        action = CompanionService.ACTION_START_SERVICE
      }
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(serviceIntent)
        } else {
          context.startService(serviceIntent)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to start CompanionService from BootReceiver", e)
      }
    }
  }

  companion object {
    private const val TAG = "BootReceiver"
  }
}

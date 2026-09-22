package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Background companion service running to maintain connection with paired parent device.
 */
class CompanionService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "CompanionService onCreate")
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "CompanionService onStartCommand with action: ${intent?.action}")

    // TODO: Implement a foreground notification showing 'Protected by Parent Control'.
    val notification = buildForegroundNotification()
    startForeground(NOTIFICATION_ID, notification)

    _isRunning.value = true

    // TODO: Implement Shizuku permission check and request. Add dependencies: dev.rikka.shizuku:api:13.1.5 and dev.rikka.shizuku:provider:13.1.5
    checkShizukuPermission()

    // TODO: Implement WebSocket or Hypercore listener for parent commands.
    startCommandListener()

    return START_STICKY
  }

  private fun checkShizukuPermission() {
    // TODO: Implement Shizuku permission check and request. Add dependencies: dev.rikka.shizuku:api:13.1.5 and dev.rikka.shizuku:provider:13.1.5
    Log.d(TAG, "Checking Shizuku permission status...")
  }

  private fun startCommandListener() {
    // TODO: Implement WebSocket or Hypercore listener for parent commands.
    Log.d(TAG, "Starting WebSocket/Hypercore listener for incoming parent instructions...")
  }

  fun handleParentCommand(command: String, targetPackage: String) {
    when (command.lowercase()) {
      "block app", "block" -> {
        // TODO: On receiving 'block app' command, execute: pm disable-user --user 0 <package> via Shizuku.
        Log.i(TAG, "Received 'block app' command for package: $targetPackage")
      }
      "unblock app", "unblock" -> {
        // TODO: On receiving 'unblock app' command, execute: pm enable <package> via Shizuku.
        Log.i(TAG, "Received 'unblock app' command for package: $targetPackage")
      }
      else -> {
        Log.w(TAG, "Unknown parent command received: $command")
      }
    }
  }

  private fun buildForegroundNotification(): Notification {
    // TODO: Implement a foreground notification showing 'Protected by Parent Control'.
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(getString(R.string.notification_title))
      .setContentText(getString(R.string.notification_content))
      .setSmallIcon(R.drawable.ic_notification_shield)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setContentIntent(pendingIntent)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = getString(R.string.notification_channel_desc)
        setShowBadge(false)
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _isRunning.value = false
    serviceScope.cancel()
    Log.d(TAG, "CompanionService onDestroy")
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    const val TAG = "CompanionService"
    const val ACTION_START_SERVICE = "com.example.service.ACTION_START"
    const val NOTIFICATION_ID = 2001
    const val CHANNEL_ID = "child_companion_channel"

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _pairingStatus = MutableStateFlow("Not Paired")
    val pairingStatus: StateFlow<String> = _pairingStatus.asStateFlow()

    fun updatePairingStatus(status: String) {
      _pairingStatus.value = status
    }
  }
}

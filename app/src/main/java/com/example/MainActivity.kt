package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.CompanionService
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Start background companion service
    startCompanionService()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        RequestNotificationPermissionIfNeeded()
        CompanionScreen()
      }
    }
  }

  private fun startCompanionService() {
    val serviceIntent = Intent(this, CompanionService::class.java).apply {
      action = CompanionService.ACTION_START_SERVICE
    }
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent)
      } else {
        startService(serviceIntent)
      }
    } catch (e: Exception) {
      // Ignore background start restrictions in preview/test environments
    }
  }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
  val context = LocalContext.current
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val permission = Manifest.permission.POST_NOTIFICATIONS
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      permission
    ) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
      if (!hasPermission) {
        launcher.launch(permission)
      }
    }
  }
}

@Composable
fun CompanionScreen() {
  val isServiceRunning by CompanionService.isRunning.collectAsState()
  var showQrCode by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Status Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        // Service indicator badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                if (isServiceRunning) Color(0xFF10B981) else Color(0xFF64748B)
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isServiceRunning) "Service Active" else "Protected",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Main Center Area
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        AnimatedContent(
          targetState = showQrCode,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "pairing_qr_transition"
        ) { isShowingQr ->
          if (!isShowingQr) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              Button(
                onClick = { showQrCode = true },
                modifier = Modifier
                  .fillMaxWidth(0.88f)
                  .height(64.dp)
                  .testTag("show_pairing_qr_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode,
                  contentDescription = null,
                  modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = stringResource(R.string.show_pairing_qr_code),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
            ) {
              // QR Code Container Card
              Card(
                modifier = Modifier
                  .size(260.dp)
                  .testTag("qr_code_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.placeholder_qr_code),
                    contentDescription = "Pairing QR Code",
                    modifier = Modifier
                      .fillMaxSize()
                      .testTag("qr_code_image"),
                    contentScale = ContentScale.Fit
                  )
                }
              }

              Spacer(modifier = Modifier.height(28.dp))

              // Waiting text and progress spinner
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = stringResource(R.string.waiting_for_parent_connection),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onBackground,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("waiting_connection_text")
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              TextButton(
                onClick = { showQrCode = false },
                modifier = Modifier.testTag("hide_qr_button")
              ) {
                Text(
                  text = "Close",
                  color = MaterialTheme.colorScheme.outline
                )
              }
            }
          }
        }
      }

      // Bottom Protection Label
      Text(
        text = stringResource(R.string.notification_content),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 12.dp)
      )
    }
  }
}

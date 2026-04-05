package me.diamondforge.simpleotp.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FromImageScreen(
    onScanned: (String) -> Unit,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) {
            onBack()
            return@rememberLauncherForActivityResult
        }
        isProcessing = true
        error = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val image = InputImage.fromFilePath(context, uri)
                    val scanner = BarcodeScanning.getClient()
                    try {
                        suspendCancellableCoroutine { cont ->
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (cont.isActive) {
                                        val rawValue = barcodes.firstOrNull { barcode ->
                                            barcode.format == Barcode.FORMAT_QR_CODE &&
                                                barcode.rawValue?.startsWith("otpauth://") == true
                                        }?.rawValue
                                        cont.resume(rawValue)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    if (cont.isActive) cont.resumeWithException(e)
                                }
                        }
                    } finally {
                        scanner.close()
                    }
                }
            }
            isProcessing = false
            result.fold(
                onSuccess = { rawValue ->
                    if (rawValue != null) {
                        onScanned(rawValue)
                    } else {
                        error = context.getString(R.string.qr_not_found_in_image)
                    }
                },
                onFailure = {
                    error = context.getString(R.string.qr_not_found_in_image)
                },
            )
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isProcessing -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.processing_image),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    ) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { launcher.launch("image/*") }) {
                            Text(stringResource(R.string.retry))
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onManualEntry) {
                            Text(stringResource(R.string.enter_manually))
                        }
                    }
                }
            }
        }
    }
}

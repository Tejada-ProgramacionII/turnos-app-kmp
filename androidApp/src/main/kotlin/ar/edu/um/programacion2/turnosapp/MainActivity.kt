package ar.edu.um.programacion2.turnosapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    private var sinRedLocal by mutableStateOf(false)

    private val pedirRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        sinRedLocal = !concedido
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (!tieneRedLocal()) {
            pedirRedLocal.launch(REDLOCAL)
        }
        setContent {
            if (sinRedLocal) {
                SinAccesoARedLocal(onPermitir = ::volverAPedirRedLocal)
            } else {
                App()
            }
        }
    }

    /** El permiso se puede haber concedido desde los ajustes del sistema. */
    override fun onResume() {
        super.onResume()
        if (sinRedLocal && tieneRedLocal()) {
            sinRedLocal = false
        }
    }

    /** Desde Android 17 los servicios en IPs privadas (10.0.2.2, ZeroTier) necesitan este permiso. */
    private fun tieneRedLocal(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
            checkSelfPermission(REDLOCAL) == PackageManager.PERMISSION_GRANTED

    /** Tras dos rechazos Android ya no muestra el diálogo: queda concederlo desde los ajustes. */
    private fun volverAPedirRedLocal() {
        if (shouldShowRequestPermissionRationale(REDLOCAL)) {
            pedirRedLocal.launch(REDLOCAL)
        } else {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
        }
    }

    private companion object {
        const val REDLOCAL = Manifest.permission.ACCESS_LOCAL_NETWORK
    }
}

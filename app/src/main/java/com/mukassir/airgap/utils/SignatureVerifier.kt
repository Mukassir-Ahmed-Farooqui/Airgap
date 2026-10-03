package com.mukassir.airgap.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mukassir.airgap.BuildConfig
import java.security.MessageDigest

/**
 * Verifies APK signing certificate against expected SHA-256 fingerprint.
 *
 * To generate the fingerprint for your release keystore:
 *   keytool -list -v -keystore <my.jks>
 * Copy the SHA256 fingerprint without colons (lowercase) into gradle.properties:
 *   AIRGAP_CERT_SHA256=your_sha256_fingerprint_here
 */
class SignatureVerifier(private val context: Context) {

    fun isSignedByTrustedCert(): Boolean {
        if (BuildConfig.DEBUG) return true
        val expectedCert = BuildConfig.AIRGAP_CERT_SHA256
        if (expectedCert.isBlank()) return true

        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                info.signatures ?: emptyArray()
            }
            val md = MessageDigest.getInstance("SHA-256")
            signatures.any { sig ->
                val digest = md.digest(sig.toByteArray())
                val hex = digest.joinToString("") { "%02x".format(it) }
                hex.equals(expectedCert, ignoreCase = true)
            }
        } catch (_: Exception) {
            false
        }
    }
}

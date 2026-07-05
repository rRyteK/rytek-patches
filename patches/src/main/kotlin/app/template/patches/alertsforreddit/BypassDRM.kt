package app.template.patches.revenuecat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ALERTS_FOR_REDDIT

// 1. The Entry Point: This is where the app starts the DRM check.
val checkLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V"
)

// 2. response handler
val processResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    parameters = listOf("I", "Landroid/os/Bundle;"),
    returnType = "V"
)

// 3. the paywall launcher
val startPaywallFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    parameters = listOf("Landroid/app/PendingIntent;"),
    returnType = "V"
)

val bypassDRMPatch = bytecodePatch(
    name = "Bypass Google Play Licensing (Pairip)",
    description = "Completely neutralizes the pairip DRM by killing the check at the entry point and blocking paywall intents.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        // Kill the DRM at the very first step
        checkLicenseFingerprint.method.addInstructions(
            0,
            """
            return-void
            """
        )

        // Kill the server response handler
        processResponseFingerprint.method.addInstructions(
            0,
            """
            return-void
            """
        )

        // Kill the paywall launcher (this was the one bypassing your last patch!)
        startPaywallFingerprint.method.addInstructions(
            0,
            """
            return-void
            """
        )
    }
}
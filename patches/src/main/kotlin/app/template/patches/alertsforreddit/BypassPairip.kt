package app.template.patches.revenuecat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ALERTS_FOR_REDDIT

// 1. Fingerprint the method that opens the Play Store / Error Dialog
val startErrorDialogFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startErrorDialogActivity",
    returnType = "V"
)

// 2. Fingerprint the method that force-closes the app
val closeAppFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "closeApp",
    returnType = "V"
)

val bypassDRMPatch = bytecodePatch(
    name = "Bypass Google Play Licensing",
    description = "Prevents Play Store redirects and app closures caused by pairip DRM.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        // Neutralize the Play Store redirect
        startErrorDialogFingerprint.method.addInstructions(
            0,
            """
            return-void
            """
        )

        // Neutralize the auto-close behavior
        closeAppFingerprint.method.addInstructions(
            0,
            """
            return-void
            """
        )
    }
}
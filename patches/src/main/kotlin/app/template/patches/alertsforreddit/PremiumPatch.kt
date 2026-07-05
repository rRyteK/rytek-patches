package app.template.patches.revenuecat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ALERTS_FOR_REDDIT
import com.android.tools.smali.dexlib2.AccessFlags

// 1. We create a fingerprint to find this exact method in the compiled app
val isActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z"
)

// 2. We define the patch that edits the bytecode
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Forces app to report that you have an active subscription.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        // We inject raw Smali at index 0 (the very top of the method)
        isActiveFingerprint.method.addInstructions(
            0,
            """
            # Load the number 1 (true) into register v0
            const/4 v0, 0x1
            
            # Return register v0 immediately
            return v0
            """
        )
    }
}

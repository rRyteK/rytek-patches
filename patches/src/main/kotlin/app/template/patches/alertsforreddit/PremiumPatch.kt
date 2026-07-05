package app.template.patches.revenuecat

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.fingerprint.fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ALERTS_FOR_REDDIT

// 1. We create a fingerprint to find this exact method in the compiled app
val isActiveFingerprint = fingerprint {
    custom { method, classDef ->
        // We match the class name you found in JADX
        classDef.type == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
                // We match the exact method name
                method.name == "isActive" &&
                // "()Z" means it takes no arguments and returns a Boolean (Z)
                method.descriptor == "()Z"
    }
}

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

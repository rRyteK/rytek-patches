package app.template.patches.alertsforreddit

import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ALERTS_FOR_REDDIT

val premiumSpeedUnlockPatch = resourcePatch(
    name = "Premium Speed Frequency Unlock",
    description = "Replaces the native libapp.so to bypass the Premium Speed frequency check."
) {
    compatibleWith(COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        // 1. Locate the native library directory inside the APK
        val libDir = get("lib/arm64-v8a")
        val targetFile = libDir.resolve("libapp.so")

        // 2. Load your patched .so file from your patch's resources folder
        val patchedLibStream = javaClass.classLoader.getResourceAsStream("patched_libs/arm64-v8a/libapp.so")
            ?: throw Exception("Could not find patched libapp.so in resources! Check your folder structure.")

        // 3. Overwrite the original APK file with your patched one
        targetFile.writeBytes(patchedLibStream.readBytes())

        println("Successfully replaced libapp.so with Premium Speed patched version!")
    }
}
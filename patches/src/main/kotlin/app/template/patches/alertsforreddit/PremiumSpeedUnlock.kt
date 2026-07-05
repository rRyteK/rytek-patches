package app.template.patches.alertsforreddit

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants

val unlockPremiumPatch = rawResourcePatch(
    name = "Unlock All Premium Features",
    description = "Bypasses the Premium Speed frequency check AND the 3-alert limit."
) {
    compatibleWith(Constants.COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        val libPath = "lib/arm64-v8a/libapp.so"
        val lib = get(libPath)

        if (!lib.exists()) {
            throw PatchException("$libPath not found. Apply this patch to an APK that includes the arm64-v8a native split.")
        }

        val bytes = lib.readBytes()

        // ==========================================
        // PATCH 1: Premium Speed Frequency Unlock
        // ==========================================
        val speedSignature = byteArrayOf(
            0x70, 0x37, 0x40, 0x91.toByte(),
            0x10, 0xEE.toByte(), 0x41, 0xF9.toByte(),
            0x3F, 0x00, 0x10, 0x6B,
            0xC1.toByte(), 0x09, 0x00, 0x54
        )
        val speedPatch = byteArrayOf(0x1F, 0x20, 0x03, 0xD5.toByte()) // ARM64 NOP

        val speedMatch = bytes.findUnique(speedSignature)
            ?: throw PatchException("Premium Speed frequency check signature not found.")

        speedPatch.copyInto(bytes, speedMatch + 12)
        println("Successfully NOP'd the Premium Speed gatekeeper at offset ${speedMatch + 12}!")

        // ==========================================
        // PATCH 2: Unlock Alerts Limit (3 alerts max)
        // ==========================================
        // The Signature:
        // CMP X1, #3  -> F1 00 0C 3F
        // B.LT offset -> EB 02 00 54
        val alertsSignature = byteArrayOf(
            0xF1.toByte(), 0x00, 0x0C, 0x3F,
            0xEB.toByte(), 0x02, 0x00, 0x54
        )

        // The Kill Shot:
        // We overwrite the conditional B.LT (EB 02 00 54) with an unconditional B (17 00 00 14).
        val alertsPatch = byteArrayOf(0x17, 0x00, 0x00, 0x14)

        val alertsMatch = bytes.findUnique(alertsSignature)
            ?: throw PatchException("Alerts limit signature not found.")

        // Offset by 4 to overwrite the B.LT instruction (which is the second 4-byte chunk)
        alertsPatch.copyInto(bytes, alertsMatch + 4)
        println("Successfully bypassed the 3-alert limit at offset ${alertsMatch + 4}!")

        lib.writeBytes(bytes)
        println("All native patches applied successfully!")
    }
}

// Helper function to find a unique byte sequence
private fun ByteArray.findUnique(pattern: ByteArray): Int? {
    var found: Int? = null
    val last = size - pattern.size
    outer@ for (i in 0..last) {
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) continue@outer
        }
        if (found != null) throw PatchException(
            "Signature matched more than once in libapp.so — too ambiguous to patch safely.",
        )
        found = i
    }
    return found
}
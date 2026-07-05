package app.template.patches.alertsforreddit

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants

val premiumSpeedUnlockPatch = rawResourcePatch(
    name = "Premium Speed & Limit Unlock",
    description = "Bypasses the Premium Speed frequency check and the 3-alert limit in the native Dart AOT snapshot."
) {
    compatibleWith(Constants.COMPATIBILITY_ALERTS_FOR_REDDIT)

    execute {
        val libPath = "lib/arm64-v8a/libapp.so"
        val lib = get(libPath)

        if (!lib.exists()) {
            throw PatchException(
                "$libPath not found. Apply this patch to an APK that includes the arm64-v8a native split."
            )
        }

        val bytes = lib.readBytes()
        var patchesApplied = 0

        // ==========================================
        // PATCH 1: Premium Speed Frequencies
        // ==========================================
        val signature1 = byteArrayOf(
            0x70, 0x37, 0x40, 0x91.toByte(),
            0x10, 0xEE.toByte(), 0x41, 0xF9.toByte(),
            0x3F, 0x00, 0x10, 0x6B,
            0xC1.toByte(), 0x09, 0x00, 0x54 // B.NE
        )

        val match1 = bytes.findUnique(signature1)
        if (match1 != null) {
            val patch1 = byteArrayOf(0x1F, 0x20, 0x03, 0xD5.toByte()) // NOP
            patch1.copyInto(bytes, match1 + 12)
            println("Successfully NOP'd the Premium Speed frequency gatekeeper!")
            patchesApplied++
        } else {
            println("Warning: Premium Speed frequency signature not found.")
        }

        // ==========================================
        // PATCH 2: 3-Alert Limit (Gatekeeper #2)
        // ==========================================
        val signature2 = byteArrayOf(
            0xC0.toByte(), 0x03, 0x3F, 0xD6.toByte(),
            0x01, 0x7C, 0x41, 0x93.toByte(),
            0x3F, 0x0C, 0x00, 0xF1.toByte(), // CMP X1, #3
            0xEB.toByte(), 0x02, 0x00, 0x54  // B.LT
        )

        val match2 = bytes.findUnique(signature2)
        if (match2 != null) {
            // Change B.LT (EB) to B.AL (EE) -> Branch Always
            val patch2 = byteArrayOf(0xEE.toByte(), 0x02, 0x00, 0x54)
            patch2.copyInto(bytes, match2 + 12)
            println("Successfully bypassed the 3-alert limit!")
            patchesApplied++
        } else {
            println("Warning: 3-alert limit signature not found.")
        }

        if (patchesApplied == 0) {
            throw PatchException("No signatures matched. The app version might have changed.")
        }

        lib.writeBytes(bytes)
        println("Successfully applied $patchesApplied patch(es) to libapp.so!")
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
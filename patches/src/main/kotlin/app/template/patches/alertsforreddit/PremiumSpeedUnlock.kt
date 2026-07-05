package app.template.patches.alertsforreddit

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants

val premiumSpeedUnlockPatch = rawResourcePatch(
    name = "Premium Speed Frequency Unlock",
    description = "Bypasses the Premium Speed frequency check in the native Dart AOT snapshot."
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

        // The Signature:
        // We are looking for the exact sequence of instructions immediately preceding the B.NE.
        // We know the target instruction is the B.NE you found in your Hex Editor: C1 09 00 54.
        // To be safe, we will search for the 12 bytes immediately before it + the 4 bytes of the B.NE.

        // The Signature:
        // 12 bytes of unique instructions right before the branch + the 4 bytes of the branch itself.
        val signature = byteArrayOf(
            // The 12 bytes preceding the branch
            0x70, 0x37, 0x40, 0x91.toByte(),
            0x10, 0xEE.toByte(), 0x41, 0xF9.toByte(),
            0x3F, 0x00, 0x10, 0x6B,
            // The target B.NE instruction
            0xC1.toByte(), 0x09, 0x00, 0x54
        )

        val match = bytes.findUnique(signature)
            ?: throw PatchException(
                "Premium Speed frequency check signature not found in $libPath. " +
                        "The app might have been updated and the assembly changed."
            )

        // The Kill Shot:
        // Overwrite the B.NE instruction with a NOP (No Operation).
        // The offset of the B.NE inside our signature is at index 12.
        val patch = byteArrayOf(
            0x1F, 0x20, 0x03, 0xD5.toByte() // ARM64 NOP
        )

        // Apply the patch
        patch.copyInto(bytes, match + 12)
        lib.writeBytes(bytes)

        println("Successfully NOP'd the Premium Speed gatekeeper at offset ${match + 12}!")
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
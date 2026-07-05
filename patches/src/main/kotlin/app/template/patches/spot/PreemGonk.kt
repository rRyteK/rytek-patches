package app.template.patches.spotify

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

val playerRestrictionsFingerprint = Fingerprint(
    definingClass = "Lcom/spotify/interapp/model/PlayerRestrictions;",
    name = "<init>", // Targets the constructor
    parameters = listOf("Z", "Z", "Z", "Z", "Z", "Z"), // 6 Booleans
    returnType = "V"
)

val unlockSpotifyPremiumPatch = bytecodePatch(
    name = "Spotify Infinite Skips & On-Demand",
    description = "Forces PlayerRestrictions to always allow skipping, seeking, and toggling shuffle."
) {
    compatibleWith("com.spotify.music")

    execute {
        playerRestrictionsFingerprint.method.addInstructions(
            0, // Inject at the very start of the constructor
            """
            const/4 p1, 0x1  # Force can_skip_next = true
            const/4 p2, 0x1  # Force can_skip_prev = true
            const/4 p3, 0x1  # Force can_repeat_track = true
            const/4 p4, 0x1  # Force can_repeat_context = true
            const/4 p5, 0x1  # Force can_toggle_shuffle = true
            const/4 p6, 0x1  # Force can_seek = true
            """
        )
    }
}
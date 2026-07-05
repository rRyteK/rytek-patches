package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_ALERTS_FOR_REDDIT = Compatibility(
        name = "Alerts For Reddit",
        packageName = "alertsforreddit.amandaoneal.application",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x348ceb,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )

    val COMPATIBILITY_EXAMPLE = Compatibility(
        name = "XYZ app",
        packageName = "com.example.app",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF0045,
        targets = listOf(
            AppTarget(
                version = "2.0.0"
            ),
            AppTarget(
                version = "1.0.2"
            )
        )
    )

    val COMPATIBILITY_EXAMPLE_2 = Compatibility(
        name = "ABC app",
        packageName = "com.example.app",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00FF45,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            ),
            AppTarget(
                version = "1.0.2"
            )
        )
    )

    val COMPATIBILITY_SPOTIFY = Compatibility(
        name = "Spotify",
        packageName = "com.spotify.music",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1DB954,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}

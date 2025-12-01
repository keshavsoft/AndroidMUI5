package com.example.compose.jetchat.core.navigation

import androidx.annotation.StringRes
import com.example.compose.jetchat.R

sealed class DrawerDestination(
    val key: String,
    @StringRes val labelRes: Int
) {
    data object Composers : DrawerDestination("composers", R.string.menu_composers)
    data object TestByKeshav : DrawerDestination("TestbyKeshav", R.string.menu_testbykeshav)
    data object Droidcon : DrawerDestination("droidcon-nyc", R.string.menu_droidcon)
    data object Gps : DrawerDestination("Gps", R.string.menu_gps)
    data object Sms : DrawerDestination("SMS", R.string.menu_sms)

    // 🔥 Add this block
    companion object {
        fun fromKey(key: String): DrawerDestination = when (key) {
            Composers.key -> Composers
            TestByKeshav.key -> TestByKeshav
            Droidcon.key -> Droidcon
            Gps.key -> Gps
            Sms.key -> Sms
            else -> Composers
        }
    }
}

package com.bioscankit.android.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendedAppsLoaderTest {
    @Test
    fun filtersCurrentHiddenAndMissingPlatformDestinations() {
        val document = """
            {"apps":[
              {"id":"inature","title":"iNature","googlePlayURL":"https://play.google.com/inature"},
              {"id":"rock","title":"Rock","googlePlayURL":"https://play.google.com/rock"},
              {"id":"bird","title":"Bird","appStoreURL":"https://apps.apple.com/bird"},
              {"id":"hidden","title":"Hidden","isVisible":false,"googlePlayURL":"https://play.google.com/hidden"}
            ]}
        """.trimIndent()

        val apps = RecommendedAppsLoader.load(
            json = document,
            excludingCurrentAppId = "inature",
            destinationUrlKey = "googlePlayURL",
        )

        assertEquals(listOf("rock"), apps.map { it.id })
    }
}

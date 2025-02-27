package com.javiersc.kotlin.kopy.args

import io.kotest.matchers.string.shouldNotBeBlank
import kotlin.test.Test

class KopyMeasurementStorageTest {

    @Test
    fun `check KopyMeasurementStorage data`() {
        KopyMeasurementStorage.NAME.shouldNotBeBlank()
    }
}

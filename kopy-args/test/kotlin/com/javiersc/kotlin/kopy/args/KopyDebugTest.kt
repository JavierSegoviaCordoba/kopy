package com.javiersc.kotlin.kopy.args

import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class KopyDebugTest {

    @Test
    fun `check KopyDebug data`() {
        KopyDebug.NAME.shouldNotBeBlank()
        KopyDebug.VALUE_DESCRIPTION.shouldNotBeBlank().shouldStartWith("<").shouldEndWith(">")
        KopyDebug.DESCRIPTION.shouldNotBeBlank()
    }
}

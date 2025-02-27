package com.javiersc.kotlin.kopy.args

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlin.test.Test

class KopyReportPathTest {

    @Test
    fun `check KopyReportPath data`() {
        KopyReportPath.NAME.shouldNotBeBlank()
        KopyReportPath.VALUE_DESCRIPTION.shouldNotBeBlank().shouldContain("/")
        KopyReportPath.DESCRIPTION.shouldNotBeBlank()
    }
}

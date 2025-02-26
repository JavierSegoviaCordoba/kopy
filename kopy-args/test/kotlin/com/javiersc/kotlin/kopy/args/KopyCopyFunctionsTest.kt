package com.javiersc.kotlin.kopy.args

import com.javiersc.kotlin.kopy.args.KopyCopyFunctions.Copy
import com.javiersc.kotlin.kopy.args.KopyCopyFunctions.Invoke
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows

class KopyCopyFunctionsTest {

    @Test
    fun kopy_functions() {
        KopyCopyFunctions.from(listOf("copy")).shouldContainExactly(Copy)
        KopyCopyFunctions.from(listOf("Copy")).shouldContainExactly(Copy)

        KopyCopyFunctions.from(listOf("invoke")).shouldContainExactly(Invoke)
        KopyCopyFunctions.from(listOf("Invoke")).shouldContainExactly(Invoke)

        KopyCopyFunctions.from(listOf("copy", "invoke")).shouldContainExactly(Copy, Invoke)
        KopyCopyFunctions.from(listOf("Copy", "Invoke")).shouldContainExactly(Copy, Invoke)

        KopyCopyFunctions.from(emptyList()).shouldContainExactly(Copy, Invoke)

        assertThrows<IllegalStateException> { KopyCopyFunctions.from(listOf("random")) }
            .message
            .shouldBe("Unknown 'KopyCopyFunctions'")
    }
}

package com.javiersc.kotlin.kopy.args

import com.javiersc.kotlin.kopy.args.KopyTransformFunctions.Set
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions.Update
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions.UpdateEach
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows

class KopyTransformFunctionsTest {

    @Test
    fun kopy_functions() {
        KopyTransformFunctions.from(listOf("set")).shouldContainExactly(Set)
        KopyTransformFunctions.from(listOf("Set")).shouldContainExactly(Set)

        KopyTransformFunctions.from(listOf("update")).shouldContainExactly(Update)
        KopyTransformFunctions.from(listOf("Update")).shouldContainExactly(Update)

        KopyTransformFunctions.from(listOf("updateeach")).shouldContainExactly(UpdateEach)
        KopyTransformFunctions.from(listOf("updateEach")).shouldContainExactly(UpdateEach)
        KopyTransformFunctions.from(listOf("Updateeach")).shouldContainExactly(UpdateEach)
        KopyTransformFunctions.from(listOf("UpdateEach")).shouldContainExactly(UpdateEach)

        KopyTransformFunctions.from(listOf("set", "update", "updateEach"))
            .shouldContainExactly(Set, Update, UpdateEach)
        KopyTransformFunctions.from(listOf("Set", "Update", "UpdateEach"))
            .shouldContainExactly(Set, Update, UpdateEach)

        KopyTransformFunctions.from(emptyList()).shouldContainExactly(Set, Update, UpdateEach)

        assertThrows<IllegalStateException> { KopyTransformFunctions.from(listOf("random")) }
            .message
            .shouldBe("Unknown 'KopyTransformFunction'")
    }
}

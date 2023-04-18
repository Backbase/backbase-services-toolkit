package com.backbase.bst.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory


class SsdkUtilsTest {

    @TestFactory
    fun `Clean package name`() = listOf(
        "com.test" to "com.test",
        "com.test.my-service" to "com.test.myservice",
        "com.test.service1" to "com.test.service1",
        "com.test.my-service1" to "com.test.myservice1",
        "com.test.1service" to "com.test.service",
        "com.test.1my-service1" to "com.test.myservice1",
        "com.test.@service" to "com.test.service",
        "@@.##.&&" to "com.backbase.demo",
    ).map { (input, expected) ->
        DynamicTest.dynamicTest("Final package name for $input should equal to $expected") {
            assertEquals(expected, SsdkUtils.cleanPackageName(input))
        }
    }

}

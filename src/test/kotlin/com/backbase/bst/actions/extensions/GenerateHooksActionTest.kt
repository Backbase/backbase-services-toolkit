package com.backbase.bst.actions.extensions

import com.backbase.bst.common.extensions.BehaviourExtensionsConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class GenerateHooksActionTest {

    @Test
    fun createPropertiesForBehaviorHooks1() {
        val props = GenerateHooksAction().createProperties(
            "employee-create-behavior",
            "com.backbase.petstoreservice.api.client"
        )
        assertEquals("employee-create-behavior", props[BehaviourExtensionsConstants.BEHAVIOUR_NAME])
        assertEquals("EmployeeCreateBehavior", props[BehaviourExtensionsConstants.BEHAVIOUR_NAME_CAMELCASE])
        assertEquals("com.backbase.petstoreservice.api.client", props[BehaviourExtensionsConstants.PACKAGE_NAME])
    }

    @Test
    fun createPropertiesForBehaviorHooks2() {
        val props = GenerateHooksAction().createProperties(
            "employee-create",
            "com.backbase.petstoreservice.api.client"
        )
        assertEquals("employee-create-behavior", props[BehaviourExtensionsConstants.BEHAVIOUR_NAME])
        assertEquals("EmployeeCreateBehavior", props[BehaviourExtensionsConstants.BEHAVIOUR_NAME_CAMELCASE])
        assertEquals("com.backbase.petstoreservice.api.client", props[BehaviourExtensionsConstants.PACKAGE_NAME])
    }
}
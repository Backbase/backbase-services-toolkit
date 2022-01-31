package com.backbase.bst.actions.openapi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


class SpecUtilsTest{

    @Test
    fun createPropertiesForClientTemplate() {
        val props = SpecUtils.createPropertiesForClientTemplate("pet-store-service",
                "com.backbase.petstoreservice.api.client" )
        assertEquals(props[SpecConstants.CLIENT_SERVICE_NAME_CAMELCASE],"PetStoreService")
        assertEquals(props[SpecConstants.CLIENT_SERVICE_NAME_LOWERCASE],"pet-store-service")
        assertEquals(props[SpecConstants.CLIENT_SERVICE_NAME_UPPERCASE],"PET_STORE_SERVICE")
        assertEquals(props[SpecConstants.CLIENT_API_PACKAGE_TRIM_LAST_DOT],"com.backbase.petstoreservice.api")
        assertEquals(props[SpecConstants.CLIENT_SERVICE_NAME_WITHOUT_SERVICE_CAMELCASE],"PetStore")
        assertEquals(props[SpecConstants.CLIENT_SERVICE_NAME_SINGLE_WORD_WITHOUT_SERVICE_LOWERCASE],"petstore")
    }
}
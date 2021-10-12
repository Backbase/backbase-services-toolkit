package com.backbase.bst.common

import com.intellij.openapi.module.JavaModuleType

class BackbaseSSDKModuleType : JavaModuleType() {

    val SSDK_CORE = "SSDK CORE"

    override fun getName(): String {
        return SSDK_CORE
    }
}
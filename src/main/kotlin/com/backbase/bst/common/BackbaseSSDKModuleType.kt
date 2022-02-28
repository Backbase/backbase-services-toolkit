package com.backbase.bst.common

import com.intellij.openapi.module.JavaModuleType

class BackbaseSSDKModuleType : JavaModuleType() {

    private val ssdkCore = "SSDK CORE"

    override fun getName(): String {
        return ssdkCore
    }
}
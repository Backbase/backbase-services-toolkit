package com.backbase.bst.common.extensions

import com.intellij.openapi.module.JavaModuleType

class BehaviourExtensionsModuleType : JavaModuleType() {

    val SSDK_EXTENSION = "Ssdk Behaviour Extension"

    override fun getName(): String {
        return SSDK_EXTENSION
    }
}
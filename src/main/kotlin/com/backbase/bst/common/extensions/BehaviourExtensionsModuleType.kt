package com.backbase.bst.common.extensions

import com.intellij.openapi.module.JavaModuleType

class BehaviourExtensionsModuleType : JavaModuleType() {

    private val behaviorExtensionType = "Ssdk Behaviour Extension"

    override fun getName(): String {
        return behaviorExtensionType
    }
}
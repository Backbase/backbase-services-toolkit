package com.backbase.bst.common

import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.lang.model.SourceVersion

object SsdkUtils {

    private const val defaultPackageName = "com.backbase.demo"

    fun listVersionsSsdk(): List<String> {
        return listOf("16.0.1","15.2.3")
    }

    /**
     * Taken from spring initializer
     * https://github.com/spring-io/initializr/blob/main/initializr-metadata/src/main/java/io/spring/initializr/metadata/InitializrConfiguration.java#L109
     *
     * Clean the specified package name if necessary. If the package name cannot be
     * transformed to a valid package name, the `defaultPackageName` is used
     * instead.
     * The package name cannot be cleaned if the specified `packageName` is
     * `null` or if it contains an invalid character for a class identifier.
     * @param packageName the package name
     * @return the cleaned package name
     */
    fun cleanPackageName(packageName: String): String {
        val candidate: String = candidatePackageName(packageName)
        if (StringUtils.isEmpty(candidate)) {
            return defaultPackageName
        }
        if (hasInvalidChar(candidate.replace(".",""))) {
            return defaultPackageName
        }
        return if (hasReservedKeyword(candidate)) {
            defaultPackageName
        } else {
            candidate
        }
    }

    private fun candidatePackageName(packageName: String): String {
        val elements = packageName.trim().replace("-".toRegex(), "").split("\\W+".toRegex()).toTypedArray()
        val sb = StringBuilder()
        for (element in elements) {
            val el = element.replaceFirst("^[0-9]+(?!$)".toRegex(), "")
            if (!el.matches("[0-9]+".toRegex()) && sb.isNotEmpty()) {
                sb.append(".")
            }
            sb.append(el)
        }
        return sb.toString()
    }

    private fun hasInvalidChar(text: String): Boolean {
        if (!Character.isJavaIdentifierStart(text[0])) {
            return true
        }
        if (text.length > 1) {
            for (i in 1 until text.length) {
                if (!Character.isJavaIdentifierPart(text[i])) {
                    return true
                }
            }
        }
        return false
    }

    private fun hasReservedKeyword(packageName: String): Boolean {
        return Arrays.stream(packageName.split("\\.".toRegex()).toTypedArray()).anyMatch { s: String? ->
            SourceVersion.isKeyword(
                s
            )
        }
    }
}
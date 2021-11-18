package com.backbase.bst.common

data class Library(val name: String, val artifact: List<String>, val description: String, var selected: Boolean = false)
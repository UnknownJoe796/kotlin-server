package com.ivieleague.kotlin.server.model

data class Sort(val scalar: Scalar, val ascending: Boolean = true, val nullsLast: Boolean = true)
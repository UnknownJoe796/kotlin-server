package com.ivieleague.kotlin.server.old.type

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule


object KotlinServerTypeModule : SimpleModule("KotlinServerModelsModule", Version(1, 0, 0, "", "com.ivieleague", "kotlin-server")) {
    init {
        addSerializer()
    }
}
package com.ivieleague.kotlin.server

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module

object KotlinServerModelsModule : Module() {
    override fun getModuleName(): String = "Kotlin Server"
    override fun version(): Version = Version(0, 0, 0, "", "com.ivieleague", "kotlin-server")
    override fun setupModule(context: SetupContext) {}
}
package com.ivieleague.kotlin.server

data class Output(
        val scalars: Collection<Scalar> = listOf(),
        val links: Map<Link, Output> = mapOf(),
        val multilinks: Map<MultiLink, Output> = mapOf()
)
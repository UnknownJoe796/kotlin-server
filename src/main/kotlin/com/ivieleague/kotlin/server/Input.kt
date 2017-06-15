package com.ivieleague.kotlin.server

data class Input(
        val id: String? = null,
        val scalars: Map<Scalar, Any?> = mapOf(),
        val links: Map<Link, Input> = mapOf(),
        val multilinkReplacements: Map<MultiLink, List<Input>> = mapOf(),
        val multilinkAdditions: Map<MultiLink, List<Input>> = mapOf(),
        val multilinkSubtractions: Map<MultiLink, List<Input>> = mapOf()
) {
    fun toOutput(): Output = Output(
            scalars = scalars.keys,
            links = links.entries.associate { it.key to it.value.toOutput() },
            multilinks = multilinkReplacements.entries.associate { it.key to Output(listOf(), mapOf(), mapOf()) } +
                    multilinkAdditions.entries.associate { it.key to Output(listOf(), mapOf(), mapOf()) }
    )
}
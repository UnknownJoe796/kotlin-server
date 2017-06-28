package com.ivieleague.kotlin.server.model

class MultilinkModifications(
        var additions: Collection<Write>? = null,
        var removals: Collection<Write>? = null,
        var replacements: Collection<Write>? = null
)
package com.ivieleague.kotlin.server.core

class MultilinkModifications(
        var additions: Collection<Write>? = null,
        var removals: Collection<Write>? = null,
        var replacements: Collection<Write>? = null
)
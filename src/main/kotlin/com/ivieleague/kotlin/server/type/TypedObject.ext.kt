package com.ivieleague.kotlin.server.type

@Suppress("UNCHECKED_CAST")
fun TypedObject.cast(otherType: SClass) = SimpleTypedObject(otherType).also {
    for (field in this.type.fields) {
        val untypedField = field as TypeField<Any>
        it[untypedField] = this[untypedField]
    }
}

fun TypedObject.mutate() = SimpleTypedObject(type).also {
    for (field in this.type.fields) {
        val untypedField = field as TypeField<Any>
        it[untypedField] = this[untypedField]
    }
}
package com.ivieleague.kotlin.server.type

@Suppress("UNCHECKED_CAST")
fun TypedObject.cast(otherType: SClass) = SimpleTypedObject(otherType).also {
    for (field in otherType.fields.values) {
        val untypedField = field as TypeField<Any?>
        it[untypedField] = this[untypedField]
    }
}

@Suppress("UNCHECKED_CAST")
fun TypedObject.mutate() = SimpleTypedObject(type).also {
    for (field in this.type.fields.values) {
        val untypedField = field as TypeField<Any?>
        it[untypedField] = this[untypedField]
    }
}

@Suppress("UNCHECKED_CAST")
fun MutableTypedObject.pullFrom(other: TypedObject) {
    for (field in this.type.fields.values) {
        val untypedField = field as TypeField<Any?>
        this[untypedField] = other[untypedField]
    }
}
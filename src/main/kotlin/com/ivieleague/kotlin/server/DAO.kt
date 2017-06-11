package com.ivieleague.kotlin.server

interface DAO {
    fun get(table: Table, id: String, output: Output): Instance?
    fun query(table: Table, condition: Condition, output: Output): Collection<Instance>
    fun update(table: Table, input: Input): Instance
}

data class Output(
        val values: Collection<Value>,
        val calculatedValues: Collection<CalculatedValue>,
        val links: Map<Link, Output>,
        val multilinks: Map<MultiLink, Output>
)

data class Input(
        val id: String?,
        val values: Map<Value, Any?>,
        val calculatedValues: Map<CalculatedValue, Any?>,
        val links: Map<Link, Input>,
        val multilinks: Map<MultiLink, List<Input>>
)

data class Instance(
        val table: Table,
        val id: String,
        val values: Collection<Any?>,
        val calculatedValues: Map<CalculatedValue, Any?>,
        val links: Map<Link, Instance>,
        val multilinks: Map<MultiLink, List<Instance>>
)
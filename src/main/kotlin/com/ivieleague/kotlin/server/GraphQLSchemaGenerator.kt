package com.ivieleague.kotlin.server

import com.lightningkite.kotlin.collection.Cache
import graphql.Scalars
import graphql.language.Field
import graphql.schema.*

class GraphQLSchemaGenerator(val dao: DAO, val tables: List<Table> = ArrayList<Table>()) {

    val schemaBuilder = GraphQLSchema.newSchema()

    val outputTypes = Cache<Table, GraphQLOutputType> {
        generateOutputType(it)
    }
    val inputTypes = Cache<Table, GraphQLInputType> {
        generateInputType(it)
    }

    fun GraphQLFieldDefinition.Builder.dataFetcherCatching(action: (DataFetchingEnvironment) -> Any?) = dataFetcher { environment ->
        try {
            action(environment)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateOutputType(table: Table): GraphQLOutputType {
        val builder = GraphQLObjectType.newObject()
                .name(table.tableName)
                .description(table.tableDescription)
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("id")
                        .description("The ID of the object")
                        .type(Scalars.GraphQLID)
                        .dataFetcherCatching { environment -> environment.getSource<Instance>().id }
                )
        for (prop in table.properties.values) {
            val def = GraphQLFieldDefinition.newFieldDefinition()
                    .name(prop.name)
                    .description(prop.description)
            val serverType = prop.type
            when (serverType) {
                is ServerType.TPointer -> {
                    val graphQLType = if (serverType.table == table) GraphQLTypeReference(table.tableName) else serverType.toGraphQLOutputType()
                    def.type(graphQLType)
                            .dataFetcherCatching { environment ->
                                val id = environment.getSource<Instance>().properties[prop] as String?
                                if (id == null) null
                                else {
                                    val outProperties = getOutProperties(environment, table)
                                    dao.get(table, id, outProperties)
                                }
                            }
                }
                is ServerType.TListPointers -> {
                    val graphQLType = if (serverType.table == table) GraphQLTypeReference(table.tableName) else serverType.toGraphQLOutputType()
                    def.type(GraphQLList.list(graphQLType))
                            .dataFetcherCatching { environment ->
                                @Suppress("UNCHECKED_CAST")
                                val ids = environment.getSource<Instance>().properties[prop] as Collection<String>
                                val outProperties = getOutProperties(environment, table)
                                ids.map { id -> dao.get(table, id, outProperties) }
                            }
                }
                else -> {
                    def.type(prop.type.toGraphQLOutputType())
                            .dataFetcherCatching { environment -> environment.getSource<Instance>().properties[prop] }
                }
            }
            builder.field(def)
        }
        return builder.build()
    }

    fun generateInputType(table: Table): GraphQLInputType {
        val builder = GraphQLInputObjectType.newInputObject()
                .name("Input" + table.tableName)
                .description(table.tableDescription)
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("id")
                        .description("The ID of the object.")
                        .type(Scalars.GraphQLID)
                )
        for (prop in table.properties.values) {
            val def = GraphQLInputObjectField.newInputObjectField()
                    .name(prop.name)
                    .description(prop.description)
            val serverType = prop.type
            when (serverType) {
                is ServerType.TPointer -> {
                    val graphQLType = if (serverType.table == table) GraphQLTypeReference("Input" + table.tableName) else serverType.toGraphQLInputType()
                    def.type(graphQLType)
                }
                is ServerType.TListPointers -> {
                    val graphQLType = if (serverType.table == table) GraphQLTypeReference("Input" + table.tableName) else serverType.toGraphQLInputType()
                    def.type(GraphQLList.list(graphQLType))
                }
                else -> {
                    def.type(prop.type.toGraphQLInputType())
                }
            }
            builder.field(def)
        }
        return builder.build()
    }

    fun ServerType.toGraphQLOutputType() = when (this) {
        ServerType.TBoolean -> Scalars.GraphQLBoolean
        ServerType.TByte -> Scalars.GraphQLByte
        ServerType.TShort -> Scalars.GraphQLShort
        ServerType.TInt -> Scalars.GraphQLInt
        ServerType.TLong -> Scalars.GraphQLLong
        ServerType.TFloat -> Scalars.GraphQLFloat
        ServerType.TDouble -> Scalars.GraphQLFloat
        ServerType.TString -> Scalars.GraphQLString
        is ServerType.TPointer -> {
            outputTypes[this.table]
        }
        is ServerType.TListPointers -> {
            GraphQLList.list(outputTypes[this.table])
        }
    }

    fun ServerType.toGraphQLInputType(): GraphQLInputType = when (this) {
        ServerType.TBoolean -> Scalars.GraphQLBoolean
        ServerType.TByte -> Scalars.GraphQLByte
        ServerType.TShort -> Scalars.GraphQLShort
        ServerType.TInt -> Scalars.GraphQLInt
        ServerType.TLong -> Scalars.GraphQLLong
        ServerType.TFloat -> Scalars.GraphQLFloat
        ServerType.TDouble -> Scalars.GraphQLFloat
        ServerType.TString -> Scalars.GraphQLString
        is ServerType.TPointer -> {
            inputTypes[this.table]!!
        }
        is ServerType.TListPointers -> {
            GraphQLList.list(inputTypes[this.table])
        }
    }

    fun build() = schemaBuilder.apply {
        query(GraphQLObjectType.newObject()
                .name("QueryType")
                .apply {
                    for (table in tables) {
                        field(GraphQLFieldDefinition.newFieldDefinition()
                                .name(table.tableName)
                                .description(table.tableDescription)
                                .type(outputTypes[table])
                                .argument(GraphQLArgument.newArgument()
                                        .name("id")
                                        .description("The ID of the object")
                                        .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
                                )
                                .dataFetcherCatching { environment ->
                                    val outProperties = getOutProperties(environment, table)
                                    dao.get(
                                            table = table,
                                            id = environment.arguments["id"] as String,
                                            properties = outProperties
                                    )
                                }
                        )
                    }
                    for (table in tables) {
                        field(GraphQLFieldDefinition.newFieldDefinition()
                                .name("Query" + table.tableName)
                                .description(table.tableDescription)
                                .type(GraphQLList.list(outputTypes[table]))
                                .apply {
                                    for (property in table.properties.values) {
                                        argument(GraphQLArgument.newArgument()
                                                .name(property.name)
                                                .description(property.description)
                                                .type(property.type.toGraphQLInputType())
                                        )
                                    }
                                }
                                .dataFetcherCatching { environment ->
                                    //TODO: More conditions than equals
                                    @Suppress("UNCHECKED_CAST")
                                    val queryConditions = environment.arguments.entries.mapNotNull {
                                        if (it.value == null) null
                                        else Condition.Equal(
                                                table.properties[it.key]!!, it.value
                                        )
                                    }
                                    val outProperties = getOutProperties(environment, table)
                                    dao.query(
                                            table = table,
                                            queryConditions = queryConditions,
                                            outProperties = outProperties
                                    )
                                }
                        )
                    }
                }
        )
        mutation(GraphQLObjectType.newObject()
                .name("MutationType")
                .apply {
                    for (table in tables) {
                        field(GraphQLFieldDefinition.newFieldDefinition()
                                .name(table.tableName)
                                .description(table.tableDescription)
                                .type(outputTypes[table])
                                .argument(GraphQLArgument.newArgument()
                                        .name("id")
                                        .description("The ID of the object")
                                        .type(Scalars.GraphQLID)
                                        .build()
                                )
                                .apply {
                                    for (property in table.properties.values) {
                                        argument(GraphQLArgument.newArgument()
                                                .name(property.name)
                                                .description(property.description)
                                                .type(property.type.toGraphQLInputType())
                                        ).build()
                                    }
                                }
                                .dataFetcherCatching { environment ->
                                    val outProperties = getOutProperties(environment, table)
                                    val arguments = environment.arguments
                                    recursiveSet(table, arguments, outProperties)
                                }.build()
                        )
                    }
                }
        )
    }.build()

    private fun getOutProperties(environment: DataFetchingEnvironment, table: Table): Collection<Property> {
        return environment.fields.first().selectionSet.selections.mapNotNull { (it as? Field)?.let { table.properties[it.name] } }
    }

    private fun recursiveSet(
            table: Table,
            arguments: Map<String, Any?>,
            outProperties: Collection<Property>
    ): Instance {
        val id = arguments["id"] as? String
        val inProperties = arguments.entries.asSequence()
                .mapNotNull { entry ->
                    val property = table.properties[entry.key] ?: return@mapNotNull null
                    val value = entry.value ?: return@mapNotNull null
                    property to value
                }
                .associate {
                    val property = it.first
                    val type = property.type
                    when (type) {
                        is ServerType.TListPointers -> property to (it.second as List<Map<String, Any?>>).map { recursiveSet(type.table, it, listOf()).id }
                        is ServerType.TPointer -> property to recursiveSet(type.table, it.second as Map<String, Any?>, listOf()).id
                        else -> property to it.second
                    }
                }
        return dao.set(
                table = table,
                id = id,
                inProperties = inProperties,
                outProperties = outProperties
        )
    }
}
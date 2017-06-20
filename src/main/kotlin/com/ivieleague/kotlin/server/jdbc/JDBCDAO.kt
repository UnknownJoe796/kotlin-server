package com.ivieleague.kotlin.server.jdbc

//class JBDCTableAccess(val connection:Connection, override val table: Table): TableAccess {
//
//    init{
//        connection.c
//    }
//
//    override fun get(user: Instance?, id: String, read: Read): Instance? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun update(user: Instance?, write: Write): Instance {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun delete(user: Instance?, id: String): Boolean {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//}

//class JDBCDAO(val connection: Connection, val database: String, val schema: Schema) : DAO {
//
//    fun ScalarType.mapped(): String = when (this) {
//        ScalarType.Boolean -> "BOOL"
//        ScalarType.Byte -> "SMALLINT"
//        ScalarType.Short -> "SMALLINT"
//        ScalarType.Int -> "INT"
//        ScalarType.Long -> "BIGINT"
//        ScalarType.Float -> "REAL"
//        ScalarType.Double -> "DOUBLE PRECISION"
//        ScalarType.ShortString -> "VARCHAR(255)"
//        ScalarType.LongString -> "TEXT"
//        ScalarType.Date -> "TIMESTAMP"
//        is ScalarType.Enum -> "SMALLINT"
//    }
//
//    fun mapToKotlin(scalar: Scalar, result: ResultSet): Any? {
//        val type = scalar.type
//        return when (type) {
//            ScalarType.Boolean -> result.getBoolean(scalar.name)
//            ScalarType.Byte -> result.getByte(scalar.name)
//            ScalarType.Short -> result.getShort(scalar.name)
//            ScalarType.Int -> result.getInt(scalar.name)
//            ScalarType.Long -> result.getLong(scalar.name)
//            ScalarType.Float -> result.getFloat(scalar.name)
//            ScalarType.Double -> result.getDouble(scalar.name)
//            ScalarType.ShortString -> result.getString(scalar.name)
//            ScalarType.LongString -> result.getString(scalar.name)
//            ScalarType.Date -> result.getDate(scalar.name)
//            is ScalarType.Enum -> type.enum[result.getByte(scalar.name)]
//        }
//    }
//
//    fun mapFromKotlin(property: Scalar, value: Any?): String {
//        val type = property.type
//        return when (type) {
//            ScalarType.Boolean -> (value as Boolean).toString()
//            ScalarType.Byte -> (value as Byte).toString()
//            ScalarType.Short -> (value as Short).toString()
//            ScalarType.Int -> (value as Int).toString()
//            ScalarType.Long -> (value as Long).toString()
//            ScalarType.Float -> (value as Float).toString()
//            ScalarType.Double -> (value as Double).toString()
//            ScalarType.ShortString -> "'${value}'"
//            ScalarType.LongString -> "'${value}'"
//            ScalarType.Date -> Timestamp((value as Date).time).toString()
//            is ScalarType.Enum -> (value as ServerEnum.Value).value.toString()
//        }
//    }
//
//    fun createTable(table: Table) {
//        connection.createStatement().executeQuery(
//                """CREATE TABLE ${table.tableName}(
//                    ID INT  SERIAL PRIMARY KEY,""" +
//                        table.properties.values.joinToString(",\n") {
//                            it.run { "$name ${type.mapped()}" }
//                        } + """,
//                    PRIMARY KEY (ID)
//                )"""
//        )
//    }
//
//    override fun get(table: Table, id: String, output: Output): Instance? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun query(table: Table, condition: Condition, output: Output): Collection<Instance> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun update(table: Table, input: Input): Instance {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    //    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
////        val result = connection.createStatement().executeQuery("SELECT id, ${properties.joinToString { it.name }} FROM ${table.tableName} WHERE id = ${id.toLong()}")
////        if (!result.next()) return null
////        return Instance(table, id, properties.associate {
////            it to mapToKotlin(it, result)
////        })
////    }
////
////    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
////        if (id != null) {
////            connection.createStatement().executeUpdate("UPDATE ${table.tableName} SET ${inProperties.entries.joinToString {
////                val type = it.key.type
////                it.key.name + " = " + mapFromKotlin(it.key, it.value)
////            }} WHERE id = ${id.toLong()};")
////            return get(table, id, outProperties)!!
////        } else {
////            val properties = inProperties.toList()
////            val statement = connection.createStatement()
////            statement.executeUpdate("INSERT INTO ${table.tableName} (${properties.joinToString { it.first.name }}) VALUES (${properties.joinToString {
////                mapFromKotlin(it.first, it.second)
////            }});")
////            val generatedKeys = statement.generatedKeys
////            generatedKeys.next()
////            return get(table, generatedKeys.getInt("id").toString(), outProperties)!!
////        }
////    }
////
////    override fun query(table: Table, queryConditions: List<Condition>, outProperties: Collection<Property>): Collection<Instance> {
////        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
////    }
//
//}
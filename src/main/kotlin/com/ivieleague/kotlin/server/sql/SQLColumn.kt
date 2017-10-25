package com.ivieleague.kotlin.server.sql

data class SQLColumn(
        val name: String,
        val description: String,
        val type: SQLDataType,
        val modifiers: Collection<Modifier>
) {
    sealed class Modifier {
        class Default(val value: String) : Modifier() {
            override fun toString(): String = "DEFAULT $value"
        }

        object NotNull : Modifier() {
            override fun toString(): String = "NOTNULL"
        }

        object Unique : Modifier() {
            override fun toString(): String = "UNIQUE"
        }

        object PrimaryKey : Modifier() {
            override fun toString(): String = "PRIMARY KEY"
        }

        class ForeignKey(
                val otherTable: String,
                val otherColumn: String,
                val onDelete: ForeignKey.Action = ForeignKey.Action.Cascade,
                val onUpdate: ForeignKey.Action = ForeignKey.Action.Cascade
        ) : Modifier() {
            override fun toString(): String = "REFERENCES $otherTable ($otherColumn) ON DELETE $onDelete ON UPDATE $onUpdate"
            sealed class Action {
                object Restrict : Action() {
                    override fun toString(): String = "RESTRICT"
                }

                object Cascade : Action() {
                    override fun toString(): String = "CASCADE"
                }

                object SetNull : Action() {
                    override fun toString(): String = "SET NULL"
                }

                object SetDefault : Action() {
                    override fun toString(): String = "SET DEFAULT"
                }
            }
        }
    }

    fun toDefineString(): String = "$name $type ${modifiers.joinToString(" ")}"
    override fun toString(): String = name
}
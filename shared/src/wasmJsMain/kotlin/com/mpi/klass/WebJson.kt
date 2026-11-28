package com.mpi.klass

internal sealed interface JValue {
    data class JObj(val entries: LinkedHashMap<String, JValue> = linkedMapOf()) : JValue
    data class JArr(val items: MutableList<JValue> = mutableListOf()) : JValue
    data class JStr(val value: String) : JValue
    data class JNum(val raw: String, val doubleValue: Double) : JValue
    data class JBool(val value: Boolean) : JValue
    data object JNull : JValue
}

internal fun JValue?.asObj(): JValue.JObj? = this as? JValue.JObj
internal fun JValue?.asArr(): JValue.JArr? = this as? JValue.JArr

internal fun JValue?.asStr(default: String = ""): String = when (this) {
    is JValue.JStr -> value
    is JValue.JNum -> raw
    is JValue.JBool -> value.toString()
    else -> default
}

internal fun JValue?.asLong(default: Long = 0L): Long = when (this) {
    is JValue.JNum -> raw.toLongOrNull() ?: doubleValue.toLong()
    is JValue.JStr -> value.trim().toLongOrNull() ?: value.trim().toDoubleOrNull()?.toLong() ?: default
    else -> default
}

internal fun JValue?.asInt(default: Int = 0): Int = when (this) {
    is JValue.JNum -> raw.toIntOrNull() ?: doubleValue.toInt()
    is JValue.JStr -> value.trim().toIntOrNull() ?: value.trim().toDoubleOrNull()?.toInt() ?: default
    else -> default
}

internal fun JValue?.asBool(default: Boolean = false): Boolean = when (this) {
    is JValue.JBool -> value
    is JValue.JStr -> when (value.trim().lowercase()) {
        "true" -> true
        "false" -> false
        else -> default
    }
    is JValue.JNum -> doubleValue != 0.0
    else -> default
}

internal fun JValue.JObj?.optStr(key: String, default: String = ""): String =
    this?.entries?.get(key).asStr(default)

internal fun JValue.JObj?.optNullableStr(key: String): String? =
    this?.entries?.get(key)?.let { v ->
        if (v is JValue.JNull) null else v.asStr("").ifBlank { null }
    }

internal fun JValue.JObj?.optLong(key: String, default: Long = 0L): Long =
    this?.entries?.get(key).asLong(default)

internal fun JValue.JObj?.optInt(key: String, default: Int = 0): Int =
    this?.entries?.get(key).asInt(default)

internal fun JValue.JObj?.optBool(key: String, default: Boolean = false): Boolean =
    this?.entries?.get(key).asBool(default)

internal fun JValue.JObj?.optObj(key: String): JValue.JObj? =
    this?.entries?.get(key).asObj()

internal fun JValue.JObj?.optArr(key: String): JValue.JArr? =
    this?.entries?.get(key).asArr()

internal class JObjBuilder {
    val map = linkedMapOf<String, JValue>()
    fun put(key: String, value: String) { map[key] = JValue.JStr(value) }
    fun put(key: String, value: Int) { map[key] = JValue.JNum(value.toString(), value.toDouble()) }
    fun put(key: String, value: Long) { map[key] = JValue.JNum(value.toString(), value.toDouble()) }
    fun put(key: String, value: Boolean) { map[key] = JValue.JBool(value) }
    fun put(key: String, value: JValue) { map[key] = value }
    fun putNullable(key: String, value: String?) {
        if (value != null) map[key] = JValue.JStr(value)
    }
    fun build(): JValue.JObj = JValue.JObj(map)
}

internal inline fun jObj(block: JObjBuilder.() -> Unit): JValue.JObj =
    JObjBuilder().apply(block).build()

internal fun jStrArr(items: Iterable<String>): JValue.JArr =
    JValue.JArr(items.mapTo(mutableListOf()) { JValue.JStr(it) })

internal fun jObjArr(items: Iterable<JValue>): JValue.JArr =
    JValue.JArr(items.toMutableList())

internal object WebJson {
    fun parse(raw: String?): JValue? {
        if (raw.isNullOrBlank()) return null
        return try {
            Parser(raw).parseRoot()
        } catch (_: Throwable) {
            null
        }
    }

    fun parseObj(raw: String?): JValue.JObj? = parse(raw) as? JValue.JObj
    fun parseArr(raw: String?): JValue.JArr? = parse(raw) as? JValue.JArr

    fun stringify(value: JValue): String = buildString {
        appendJson(value)
    }

    private fun StringBuilder.appendJson(value: JValue) {
        when (value) {
            is JValue.JNull -> append("null")
            is JValue.JBool -> append(if (value.value) "true" else "false")
            is JValue.JNum -> append(value.raw)
            is JValue.JStr -> appendQuoted(value.value)
            is JValue.JArr -> {
                append('[')
                value.items.forEachIndexed { idx, item ->
                    if (idx > 0) append(',')
                    appendJson(item)
                }
                append(']')
            }
            is JValue.JObj -> {
                append('{')
                var first = true
                for ((k, v) in value.entries) {
                    if (!first) append(',')
                    first = false
                    appendQuoted(k)
                    append(':')
                    appendJson(v)
                }
                append('}')
            }
        }
    }

    private fun StringBuilder.appendQuoted(str: String) {
        append('"')
        for (ch in str) {
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (ch.code < 0x20) {
                        append("\\u")
                        append(ch.code.toString(16).padStart(4, '0'))
                    } else {
                        append(ch)
                    }
                }
            }
        }
        append('"')
    }

    private class Parser(private val s: String) {
        private var pos = 0
        private val len = s.length

        fun parseRoot(): JValue {
            skipWs()
            val v = parseValue()
            skipWs()
            return v
        }

        private fun skipWs() {
            while (pos < len && s[pos].isWhitespace()) pos++
        }

        private fun parseValue(): JValue {
            skipWs()
            if (pos >= len) throw IllegalArgumentException("Unexpected EOF")
            return when (val c = s[pos]) {
                '{' -> parseObj()
                '[' -> parseArr()
                '"' -> JValue.JStr(parseString())
                't' -> expectLiteral("true", JValue.JBool(true))
                'f' -> expectLiteral("false", JValue.JBool(false))
                'n' -> expectLiteral("null", JValue.JNull)
                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalArgumentException("Unexpected char $c at $pos")
            }
        }

        private fun parseObj(): JValue.JObj {
            pos++ // '{'
            val map = linkedMapOf<String, JValue>()
            skipWs()
            if (pos < len && s[pos] == '}') {
                pos++
                return JValue.JObj(map)
            }
            while (true) {
                skipWs()
                if (pos >= len || s[pos] != '"') throw IllegalArgumentException("Expected key at $pos")
                val key = parseString()
                skipWs()
                if (pos >= len || s[pos] != ':') throw IllegalArgumentException("Expected ':' at $pos")
                pos++
                val value = parseValue()
                map[key] = value
                skipWs()
                if (pos >= len) throw IllegalArgumentException("Unterminated object")
                when (s[pos]) {
                    ',' -> pos++
                    '}' -> {
                        pos++
                        return JValue.JObj(map)
                    }
                    else -> throw IllegalArgumentException("Expected ',' or '}' at $pos")
                }
            }
        }

        private fun parseArr(): JValue.JArr {
            pos++ // '['
            val list = mutableListOf<JValue>()
            skipWs()
            if (pos < len && s[pos] == ']') {
                pos++
                return JValue.JArr(list)
            }
            while (true) {
                list.add(parseValue())
                skipWs()
                if (pos >= len) throw IllegalArgumentException("Unterminated array")
                when (s[pos]) {
                    ',' -> pos++
                    ']' -> {
                        pos++
                        return JValue.JArr(list)
                    }
                    else -> throw IllegalArgumentException("Expected ',' or ']' at $pos")
                }
            }
        }

        private fun parseString(): String {
            pos++ // '"'
            val sb = StringBuilder()
            while (pos < len) {
                val c = s[pos++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (pos >= len) throw IllegalArgumentException("Unterminated escape")
                        when (val esc = s[pos++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (pos + 4 > len) throw IllegalArgumentException("Bad unicode escape")
                                val code = s.substring(pos, pos + 4).toInt(16)
                                sb.append(code.toChar())
                                pos += 4
                            }
                            else -> sb.append(esc)
                        }
                    }
                    else -> sb.append(c)
                }
            }
            throw IllegalArgumentException("Unterminated string")
        }

        private fun expectLiteral(lit: String, value: JValue): JValue {
            if (s.startsWith(lit, pos)) {
                pos += lit.length
                return value
            }
            throw IllegalArgumentException("Expected $lit at $pos")
        }

        private fun parseNumber(): JValue.JNum {
            val start = pos
            if (s[pos] == '-') pos++
            while (pos < len && s[pos] in '0'..'9') pos++
            if (pos < len && s[pos] == '.') {
                pos++
                while (pos < len && s[pos] in '0'..'9') pos++
            }
            if (pos < len && (s[pos] == 'e' || s[pos] == 'E')) {
                pos++
                if (pos < len && (s[pos] == '+' || s[pos] == '-')) pos++
                while (pos < len && s[pos] in '0'..'9') pos++
            }
            val raw = s.substring(start, pos)
            val d = raw.toDoubleOrNull() ?: 0.0
            return JValue.JNum(raw, d)
        }
    }
}

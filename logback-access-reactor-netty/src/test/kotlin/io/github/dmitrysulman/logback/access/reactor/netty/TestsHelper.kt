package io.github.dmitrysulman.logback.access.reactor.netty

val headerList =
    listOf(
        object : Map.Entry<CharSequence, CharSequence> {
            override val key = "name1"
            override val value = "value1"
        },
        object : Map.Entry<CharSequence, CharSequence> {
            override val key = "name2"
            override val value = "value2"
        },
        object : Map.Entry<CharSequence, CharSequence> {
            override val key = "NAME3"
            override val value = "value3"
        },
        object : Map.Entry<CharSequence, CharSequence> {
            override val key = ""
            override val value = "empty_name"
        },
        object : Map.Entry<CharSequence?, CharSequence> {
            override val key = null
            override val value = "null_name"
        },
        object : Map.Entry<CharSequence, CharSequence> {
            override val key = "empty_value"
            override val value = ""
        },
        object : Map.Entry<CharSequence, CharSequence?> {
            override val key = "null_value"
            override val value = null
        },
    )

fun headerListIterator() = headerList.iterator()

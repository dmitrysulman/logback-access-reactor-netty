package io.dmitrysulman.logback.access.reactor.netty

// TODO remove
class HeaderMapAdapter(
    private val getHeader: (String) -> String?
) : Map<String, String> {
    override val entries: Set<Map.Entry<String, String>> = emptySet()
    override val keys: Set<String> = emptySet()
    override val size = 0
    override val values: Collection<String> = emptyList()

    override fun isEmpty() = false

    override fun get(key: String) = getHeader(key)

    override fun containsValue(value: String) = false

    override fun containsKey(key: String) = get(key) != null
}
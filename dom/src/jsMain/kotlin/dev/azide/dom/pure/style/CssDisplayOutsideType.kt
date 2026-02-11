package dev.azide.dom.pure.style

sealed class CssDisplayOutsideType(
    override val cssDisplayString: String,
) : CssDisplayType() {
    companion object {
        fun parse(
            type: String,
        ): CssDisplayOutsideType = when (type.lowercase()) {
            Block.cssDisplayString -> Block
            Inline.cssDisplayString -> Inline
            else -> throw IllegalArgumentException("Unknown display-outside type: $type")
        }
    }

    data object Block : CssDisplayOutsideType("block")
    data object Inline : CssDisplayOutsideType("inline")
}

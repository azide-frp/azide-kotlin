package dev.azide.dom.pure

import dev.azide.dom.pure.style.CssPropertyValue

data class CssColor(
    val red: Int,
    val green: Int,
    val blue: Int,
) : CssPropertyValue() {
    companion object {
        val darkBlue = CssColor(0, 0, 139)

        val black = CssColor(0, 0, 0)

        val red = CssColor(255, 0, 0)

        val green = CssColor(0, 255, 0)

        val lightBlue = CssColor(173, 216, 230)

        val blue = CssColor(0, 0, 255)

        val lightGray = CssColor(211, 211, 211)

        val darkGray = CssColor(169, 169, 169)
    }

    init {
        require(red in 0..255) { "Red value must be between 0 and 255" }
        require(green in 0..255) { "Green value must be between 0 and 255" }
        require(blue in 0..255) { "Blue value must be between 0 and 255" }
    }

    val value: Int
        get() = (red shl 16) or (green shl 8) or blue

    override val cssString: String
        get() = "rgb($red, $green, $blue)"
}

package dev.azide.dom.pure

import dev.azide.dom.pure.style.CssPropertyValue

data class CssDimension<out U : CssUnit>(
    val value: Double,
    val unit: U,
) : CssPropertyValue() {
    companion object {
        private val regex = Regex("([0-9.]+)([a-zA-Z%]+)")

        fun parse(
            string: String,
        ): CssDimension<*> {
            val matchResult =
                regex.matchEntire(string) ?: throw IllegalArgumentException("Invalid dimension format: $string")

            val (valueString, unitString) = matchResult.destructured

            val value = valueString.toDouble()
            val unit = CssUnit.parse(unitString)

            return CssDimension(
                value = value,
                unit = unit,
            )
        }
    }

    val asAbsolute: CssDimension<CssUnit.Absolute>?
        get() {
            val absoluteUnit = unit as? CssUnit.Absolute ?: return null

            return CssDimension(
                value = value,
                unit = absoluteUnit,
            )
        }

    override val cssString: String
        get() = "$value${unit.string}"
}

fun <U : CssUnit.Absolute> CssDimension<CssUnit.Absolute>.inUnit(
    otherUnit: U,
): CssDimension<U> = CssDimension(
    value = value * otherUnit.per(unit),
    unit = otherUnit,
)

val Double.mm: CssDimension<CssUnit.Mm>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Mm,
    )

val Int.mm: CssDimension<CssUnit.Mm>
    get() = this.toDouble().mm

val Double.inch: CssDimension<CssUnit.Inch>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Inch,
    )

val Int.inch: CssDimension<CssUnit.Inch>
    get() = this.toDouble().inch

val Double.pt: CssDimension<CssUnit.Pt>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Pt,
    )

val Int.pt: CssDimension<CssUnit.Pt>
    get() = this.toDouble().pt


val Double.px: CssDimension<CssUnit.Px>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Px,
    )

val Int.px: CssDimension<CssUnit.Px>
    get() = this.toDouble().px

val Double.percent: CssDimension<CssUnit.Percent>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Percent,
    )

val Int.percent: CssDimension<CssUnit.Percent>
    get() = this.toDouble().percent

val Double.vw: CssDimension<CssUnit.Vw>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Vw,
    )

val Int.vw: CssDimension<CssUnit.Vw>
    get() = this.toDouble().vw

val Double.vh: CssDimension<CssUnit.Vh>
    get() = CssDimension(
        value = this,
        unit = CssUnit.Vh,
    )

val Int.vh: CssDimension<CssUnit.Vh>
    get() = this.toDouble().vh

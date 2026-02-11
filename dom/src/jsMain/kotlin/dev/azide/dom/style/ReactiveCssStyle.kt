package dev.azide.dom.style

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Schedule
import dev.azide.core.Schedules
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.startingOf
import dev.azide.dom.pure.CssColor
import dev.azide.dom.pure.CssDimension
import dev.azide.dom.pure.style.CssBorderStyle
import dev.azide.dom.pure.style.CssBoxSizing
import dev.azide.dom.pure.style.CssDisplayStyle
import dev.azide.dom.pure.style.CssFill
import dev.azide.dom.pure.style.CssFlexItemStyle
import dev.azide.dom.pure.style.CssPointerEvents
import dev.azide.dom.pure.style.CssPosition
import dev.azide.dom.pure.style.CssPropertyKind
import dev.azide.dom.pure.style.CssStrokeStyle
import dev.azide.dom.pure.style.CssTextAlign
import dev.azide.dom.pure.style.CssVerticalAlign
import org.w3c.dom.css.CSSStyleDeclaration

data class ReactiveCssStyle(
    val flexItemStyle: CssFlexItemStyle? = null,
    val displayStyle: Cell<CssDisplayStyle>? = null,
    val width: Cell<CssDimension<*>>? = null,
    val height: Cell<CssDimension<*>>? = null,
    val minWidth: Cell<CssDimension<*>>? = null,
    val minHeight: Cell<CssDimension<*>>? = null,
    val backgroundColor: Cell<CssColor>? = null,
    val textAlign: Cell<CssTextAlign>? = null,
    val verticalAlign: Cell<CssVerticalAlign>? = null,
    val borderStyle: CssBorderStyle? = null,
    val boxSizing: CssBoxSizing? = null,
    val fill: Cell<CssFill>? = null,
    val strokeStyle: CssStrokeStyle? = null,
    val pointerEvents: Cell<CssPointerEvents>? = null,
    val padding: CssEdgeInsets? = null,
    val margin: CssEdgeInsets? = null,
    val position: CssPosition? = null,
    val inset: CssInset? = null,
) {
    companion object {
        val Default = ReactiveCssStyle()
    }

    fun bind(
        styleDeclaration: CSSStyleDeclaration,
    ): Schedule = Action.adapt(
        externalTrigger = object : ExternalTrigger {
            override fun executeExternally() {
                flexItemStyle?.applyTo(
                    styleDeclaration = styleDeclaration,
                )

                borderStyle?.applyTo(
                    styleDeclaration = styleDeclaration,
                )

                boxSizing?.applyTo(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.BoxSizing,
                )

                strokeStyle?.applyTo(
                    styleDeclaration = styleDeclaration,
                )

                margin?.applyProperties(
                    insetKind = CssEdgeInsets.InsetKind.Margin,
                    applier = StyleDeclarationApplier(
                        styleDeclaration = styleDeclaration,
                    ),
                )

                padding?.applyProperties(
                    insetKind = CssEdgeInsets.InsetKind.Padding,
                    applier = StyleDeclarationApplier(
                        styleDeclaration = styleDeclaration,
                    ),
                )

                position?.applyTo(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.Position,
                )

                inset?.applyProperties(
                    applier = StyleDeclarationApplier(
                        styleDeclaration = styleDeclaration,
                    ),
                )
            }
        },
    ).startingOf {
        Schedules.combine(
            schedules = listOfNotNull(
                displayStyle?.bind(
                    styleDeclaration = styleDeclaration,
                ),
                width?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.Width,
                ),
                height?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.Height,
                ),
                minWidth?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.MinWidth,
                ),
                minHeight?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.MinHeight,
                ),
                backgroundColor?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.BackgroundColor,
                ),
                textAlign?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.TextAlign,
                ),
                verticalAlign?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.VerticalAlign,
                ),
                fill?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.Fill,
                ),
                pointerEvents?.bind(
                    styleDeclaration = styleDeclaration,
                    kind = CssPropertyKind.PointerEvents,
                ),
            ),
        )
    }
}

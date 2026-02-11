package com.example.azide_example

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.accumulating
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveList
import dev.azide.core.collections.ReactiveSet
import dev.azide.core.collections.actuateOf
import dev.azide.core.collections.helpers.withReactiveSortKey
import dev.azide.core.collections.sortedUniquelyReactively
import dev.azide.core.executeEachOf
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.joinOf
import dev.azide.core.map
import dev.azide.core.startExternally
import dev.azide.dom.creatingReactiveHtmlDivElement
import dev.azide.dom.creatingReactiveText
import dev.azide.dom.intervalTimeoutEffect
import dev.azide.dom.pure.CssColor
import dev.azide.dom.pure.px
import dev.azide.dom.pure.style.CssBorderStyle
import dev.azide.dom.pure.style.CssFlexAlignItems
import dev.azide.dom.pure.style.CssFlexDirection
import dev.azide.dom.pure.style.CssFlexStyle
import dev.azide.dom.style.ReactiveCssStyle
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Text
import kotlin.time.Duration.Companion.seconds

fun buildingGrowingMagicNumber(
    seedValue: Int,
): Effect<Cell<Int>> = window.intervalTimeoutEffect(
    intervalDuration = seedValue.seconds,
).joinOf { ticks ->
    ticks.accumulating(
        initialAccValue = seedValue,
    ) { n, _: Unit -> n + 1 }
}

fun creatingEntityElement(
    entity: Entity,
): Effect<HTMLDivElement> = document.creatingReactiveText(
    data = entity.magicNumber.map { magicNumberNow: Int ->
        magicNumberNow.toString()
    },
).joinOf { magicNumberTextNode: Text ->
    document.creatingReactiveHtmlDivElement(
        children = ReactiveList.of(
            magicNumberTextNode,
        ),
        style = ReactiveCssStyle(
            displayStyle = Cell.Const(
                CssFlexStyle(
                    direction = CssFlexDirection.Row,
                    alignItems = CssFlexAlignItems.Center,
                ),
            ),
            width = Cell.Const(32.px),
            borderStyle = CssBorderStyle(
                width = 1.px,
                color = CssColor.red,
                style = CssBorderStyle.Style.Solid,
            ),
        ),
    )
}

fun creatingRootElement(
    app: App,
): Effect<HTMLDivElement> = app.entities.actuateOf { entity: Entity ->
    creatingEntityElement(entity = entity).map { entityElement: HTMLElement ->
        entityElement withReactiveSortKey entity.magicNumber
    }
}.joinOf { sortableMagicNumberElements ->
    val sortedMagicNumberElements: ReactiveList<HTMLElement> = sortableMagicNumberElements.sortedUniquelyReactively()

    document.creatingReactiveHtmlDivElement(
        children = sortedMagicNumberElements,
        style = ReactiveCssStyle(
            displayStyle = Cell.Const(
                CssFlexStyle(
                    direction = CssFlexDirection.Row,
                    gap = 8.px,
                ),
            ),
        ),
    )
}

class Entity(
    val magicNumber: Cell<Int>,
) {
    companion object {
        fun create(
            seedValue: Int,
        ): Effect<Entity> = buildingGrowingMagicNumber(seedValue).map { magicNumber ->
            // FIXME: Needs a Construction context semantically
            Entity(
                magicNumber = magicNumber,
            )
        }
    }
}

class App private constructor(
    val entities: ReactiveBag<Entity>,
) {
    companion object {
        fun create(): Effect<App> = ReactiveSet.Const(
            setOf(1, 2, 3),
        ).actuateOf { seedValue: Int ->
            Entity.create(seedValue = seedValue)
        }.map { entities: ReactiveBag<Entity> ->
            App(
                entities = entities,
            )
        }
    }
}

fun main() {
    val timeoutStream = window.intervalTimeoutEffect(
        intervalDuration = 1.seconds,
    ).startExternally().result

    timeoutStream.executeEachOf {
        Action.adapt(
            externalTrigger = object : ExternalTrigger {
                override fun executeExternally() {
                    println("Timeout!")
                }
            },
        )
    }.startExternally()

    val rootElement = App.create().joinOf { app: App ->
        creatingRootElement(
            app = app,
        )
    }.startExternally().result

    document.body!!.append(rootElement)
}

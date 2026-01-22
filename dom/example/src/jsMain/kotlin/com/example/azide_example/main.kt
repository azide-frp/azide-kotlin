package com.example.azide_example

import dev.azide.core.Action
import dev.azide.core.executeEachOf
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.startExternally
import dev.azide.dom.intervalTimeoutEffect
import kotlinx.browser.window
import kotlin.time.Duration.Companion.seconds

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
}

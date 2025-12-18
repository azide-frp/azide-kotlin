package dev.azide.core.test_utils.event_stream

import dev.kmpx.platform.PlatformWeakReference
import dev.azide.core.EventStream
import dev.azide.core.test_utils.JsGarbageCollectorUtils.awaitCollection

internal object JsEventStreamTestUtils {
    suspend fun <EventT> ensureCollectible(
        buildEventStream: () -> EventStream<EventT>,
    ) {
        val (subjectEventStreamWeakRef, subjectVertexWeakRef) = run {
            val subjectEventStream = buildEventStream()

            Pair(
                PlatformWeakReference(subjectEventStream), PlatformWeakReference(subjectEventStream.vertex)
            )
        }

        subjectEventStreamWeakRef.awaitCollection()

        subjectVertexWeakRef.awaitCollection()
    }
}

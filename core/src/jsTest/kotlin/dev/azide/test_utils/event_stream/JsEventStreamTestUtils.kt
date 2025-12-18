package dev.azide.test_utils.event_stream

import dev.kmpx.platform.PlatformWeakReference
import dev.azide.EventStream
import dev.azide.test_utils.JsGarbageCollectorUtils.awaitCollection

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

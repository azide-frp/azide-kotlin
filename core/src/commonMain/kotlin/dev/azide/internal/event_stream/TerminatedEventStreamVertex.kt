package dev.azide.internal.event_stream

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    override val ongoingEmission: Nothing?
        get() = null
}

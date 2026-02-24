package dev.azide.core.test_utils.generic

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.event_stream.EventStreamVertex

interface TestSubjectObservationTrait<in SubjectT, out NotificationT : Any> {
    fun extractVertex(subject: SubjectT): ListenableVertex

    fun extractOngoingNotification(subject: SubjectT): NotificationT?
}

class EventStreamObservationTrait<EventT> :
    TestSubjectObservationTrait<EventStream<EventT>, EventStreamVertex.Emission<EventT>> {
    override fun extractVertex(
        subject: EventStream<EventT>,
    ): ListenableVertex = subject.vertex

    override fun extractOngoingNotification(
        subject: EventStream<EventT>,
    ): EventStreamVertex.Emission<EventT>? = subject.vertex.ongoingEmission
}

class CellObservationTrait<ValueT> : TestSubjectObservationTrait<Cell<ValueT>, CellVertex.Update<ValueT>> {
    override fun extractVertex(
        subject: Cell<ValueT>,
    ): ListenableVertex = subject.vertex

    override fun extractOngoingNotification(
        subject: Cell<ValueT>,
    ): CellVertex.Update<ValueT>? = subject.vertex.ongoingUpdate
}

class ReactiveBagObservationTrait<ElementT> :
    TestSubjectObservationTrait<ReactiveBag<ElementT>, TaggedBagChange<ElementT>> {
    override fun extractVertex(
        subject: ReactiveBag<ElementT>,
    ): ListenableVertex = subject.trackedVertex

    override fun extractOngoingNotification(
        subject: ReactiveBag<ElementT>,
    ): TaggedBagChange<ElementT>? = subject.trackedVertex.ongoingChange
}


class ReactiveListObservationTrait<ElementT> :
    TestSubjectObservationTrait<ReactiveList<ElementT>, ListChange<ElementT>> {
    override fun extractVertex(
        subject: ReactiveList<ElementT>,
    ): ListenableVertex = subject.trackedVertex

    override fun extractOngoingNotification(
        subject: ReactiveList<ElementT>,
    ): ListChange<ElementT>? = subject.trackedVertex.ongoingChange
}

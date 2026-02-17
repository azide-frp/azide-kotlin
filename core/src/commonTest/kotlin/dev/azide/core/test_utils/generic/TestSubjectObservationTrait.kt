package dev.azide.core.test_utils.generic

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange

interface TestSubjectObservationTrait<in SubjectT, out NotificationT : Any> {
    fun extractVertex(subject: SubjectT): Vertex

    fun extractOngoingNotification(subject: SubjectT): NotificationT?
}

class ReactiveBagObservationTrait<ElementT> :
    TestSubjectObservationTrait<ReactiveBag<ElementT>, TaggedBagChange<ElementT>> {
    override fun extractVertex(
        subject: ReactiveBag<ElementT>,
    ): Vertex = subject.trackedVertex

    override fun extractOngoingNotification(
        subject: ReactiveBag<ElementT>,
    ): TaggedBagChange<ElementT>? = subject.trackedVertex.ongoingChange
}

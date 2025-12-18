package dev.azide.internal

object Transactions {
    interface PropagationContext {
        fun enqueueForCommitment(
            vertex: CommittableVertex,
        )
    }

    inline fun <ResultT> execute(
        propagate: (PropagationContext) -> ResultT,
    ): ResultT {
        val verticesToCommit = mutableListOf<CommittableVertex>()

        val result = propagate(
            object : PropagationContext {
                override fun enqueueForCommitment(
                    vertex: CommittableVertex,
                ) {
                    verticesToCommit.add(vertex)
                }
            },
        )

        verticesToCommit.forEach { vertex ->
            vertex.commit()
        }

        return result
    }
}

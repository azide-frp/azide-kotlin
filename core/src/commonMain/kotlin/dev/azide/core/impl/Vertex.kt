package dev.azide.core.impl

interface Vertex {
    enum class ActivationMode {
        /**
         * Online activation is the "full" activation mode, when the vertex is activated in the middle of the
         * propagation phase. Other vertices will expect the activated vertex to expose its volatile state. The vertex
         * should subscribe/observe its dependencies, having in mind that the propagation is still ongoing.
         */
        Online,
        /**
         * Online activation is the "quick" activation mode, when the vertex is activated after the propagation phase.
         * Other vertices won't expect the activated vertex to expose its volatile state. The vertex should subscribe/
         * observe its dependencies, having in mind that the propagation has ended and will happen again not sooner than
         * in the next transaction.
         */
        Offline,
    }
}

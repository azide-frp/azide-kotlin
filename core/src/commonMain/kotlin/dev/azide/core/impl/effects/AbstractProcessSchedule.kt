package dev.azide.core.impl.effects

abstract class AbstractProcessSchedule<ProcessVertexT> :
    AbstractProcessEffect<ProcessVertexT, Unit>() where ProcessVertexT : ProcessVertex {
    final override fun wrap(effectVertex: ProcessVertexT): Unit = Unit
}

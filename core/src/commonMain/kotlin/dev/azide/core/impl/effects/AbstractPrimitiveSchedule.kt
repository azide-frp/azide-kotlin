package dev.azide.core.impl.effects

import dev.azide.core.impl.RevocationHandle

abstract class AbstractPrimitiveSchedule<EffectVertexT> :
    AbstractPrimitiveEffect<EffectVertexT, Unit>() where EffectVertexT : EffectVertex, EffectVertexT : RevocationHandle {
    final override fun wrap(effectVertex: EffectVertexT): Unit = Unit
}

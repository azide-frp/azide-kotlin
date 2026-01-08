package dev.azide.core.impl.effects

import dev.azide.core.impl.Revocable

abstract class AbstractPrimitiveSchedule<EffectVertexT> :
    AbstractPrimitiveEffect<EffectVertexT, Unit>() where EffectVertexT : EffectVertex, EffectVertexT : Revocable {
    final override fun wrap(effectVertex: EffectVertexT): Unit = Unit
}

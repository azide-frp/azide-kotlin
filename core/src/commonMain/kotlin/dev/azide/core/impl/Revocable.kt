package dev.azide.core.impl

interface Revocable {
    object Noop : Revocable {
        override fun revoke() {
        }
    }

    companion object {
        fun combine(
            firstSubHandle: Revocable,
            secondSubHandle: Revocable,
        ): Revocable = object : Revocable {
            override fun revoke() {
                firstSubHandle.revoke()
                secondSubHandle.revoke()
            }
        }

        fun combine(
            vararg subHandles: Revocable,
        ): Revocable = object : Revocable {
            override fun revoke() {
                for (handle in subHandles) {
                    handle.revoke()
                }
            }
        }
    }

    // TODO: Add `propagationContext` argument and transaction IDs to detect attempts of revoking in later transactions
    fun revoke()
}

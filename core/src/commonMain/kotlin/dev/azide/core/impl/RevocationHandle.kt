package dev.azide.core.impl

interface RevocationHandle {
    object Noop : RevocationHandle {
        override fun revoke() {
        }
    }

    companion object {
        fun combine(
            firstSubHandle: RevocationHandle,
            secondSubHandle: RevocationHandle,
        ): RevocationHandle = object : RevocationHandle {
            override fun revoke() {
                firstSubHandle.revoke()
                secondSubHandle.revoke()
            }
        }

        fun combine(
            vararg subHandles: RevocationHandle,
        ): RevocationHandle = object : RevocationHandle {
            override fun revoke() {
                for (handle in subHandles) {
                    handle.revoke()
                }
            }
        }
    }

    fun revoke()
}

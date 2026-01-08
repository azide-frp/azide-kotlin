package dev.azide.core.impl

abstract class AbstractGuardedRevocationHandle : RevocationHandle {
    private var wasRevoked = false

    override fun revoke() {
        if (wasRevoked) throw IllegalStateException("Action was already revoked")

        wasRevoked = true

        revokeGuarded()
    }

    abstract fun revokeGuarded()
}

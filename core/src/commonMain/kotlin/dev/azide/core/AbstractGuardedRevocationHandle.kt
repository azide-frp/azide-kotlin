package dev.azide.core

abstract class AbstractGuardedRevocationHandle : Action.RevocationHandle {
    private var wasRevoked = false

    override fun revoke() {
        if (wasRevoked) throw IllegalStateException("Action was already revoked")

        wasRevoked = true

        revokeGuarded()
    }

    abstract fun revokeGuarded()
}

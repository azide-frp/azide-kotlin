package dev.azide.core.impl

abstract class AbstractGuardedRevocable : Revocable {
    private var wasRevoked = false

    override fun revoke() {
        if (wasRevoked) throw IllegalStateException("Action was already revoked")

        wasRevoked = true

        revokeGuarded()
    }

    abstract fun revokeGuarded()
}

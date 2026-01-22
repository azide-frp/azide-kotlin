package dev.azide.core.external

interface ExternalAllocator<ObjectT> {
    fun allocateExternally(): ObjectT
}

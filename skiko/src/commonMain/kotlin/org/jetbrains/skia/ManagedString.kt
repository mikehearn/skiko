package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class ManagedString internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(_nMake(s)) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            val size = _nStringSize(_ptr)
            withResult(ByteArray(size)) {
                _nStringData(_ptr, it, size)
            }.decodeToString()
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun insert(offset: Int, s: String): ManagedString {
        Stats.onNativeCall()
        _nInsert(_ptr, offset, s)
        return this
    }

    fun append(s: String): ManagedString {
        Stats.onNativeCall()
        _nAppend(_ptr, s)
        return this
    }

    fun remove(from: Int): ManagedString {
        Stats.onNativeCall()
        _nRemoveSuffix(_ptr, from)
        return this
    }

    fun remove(from: Int, length: Int): ManagedString {
        Stats.onNativeCall()
        _nRemove(_ptr, from, length)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = ManagedString_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
private external fun ManagedString_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
private external fun _nMake(s: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringSize")
private external fun _nStringSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringData")
private external fun _nStringData(ptr: NativePointer, result: InteropPointer, size: Int): String

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
private external fun _nInsert(ptr: NativePointer, offset: Int, s: String?)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
private external fun _nAppend(ptr: NativePointer, s: String?)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
private external fun _nRemoveSuffix(ptr: NativePointer, from: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
private external fun _nRemove(ptr: NativePointer, from: Int, length: Int)

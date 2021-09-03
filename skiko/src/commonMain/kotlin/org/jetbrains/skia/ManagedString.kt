@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class ManagedString internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nMake(s: String?): Long
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nToString(ptr: Long): String
        @JvmStatic external fun _nInsert(ptr: Long, offset: Int, s: String?)
        @JvmStatic external fun _nAppend(ptr: Long, s: String?)
        @JvmStatic external fun _nRemoveSuffix(ptr: Long, from: Int)
        @JvmStatic external fun _nRemove(ptr: Long, from: Int, length: Int)

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
            _nToString(_ptr)
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
        val PTR = _nGetFinalizer()
    }
}
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

fun<K : IrElement, V> irIntercepted_HashMap() = irDynamicProperty<K, V>()
fun<K : IrElement, V> irIntercepted_hashMapOf() = irIntercepted_HashMap<K, V>()

fun<K : IrElement, V> irIntercepted_LinkedHashMap() = LinkedHashMap<K, V>()

fun<K : IrElement, V> irIntercepted_MutableMap() = irDynamicProperty<K, V>()

fun<K : IrElement, V> irInterceptedLocal_HashMap() = HashMap<K, V>()
fun<K : IrElement, V> irInterceptedLocal_hashMapOf() = irInterceptedLocal_HashMap<K, V>()

fun<K : IrElement, V> irIntercepted_WeakHashMap() = irDynamicProperty<K, V>()

fun<K : IrElement, V> irIntercepted_ConcurrentHashMap() = irDynamicProperty<K, V>()

fun<K : IrElement> irIntercepted_HashSet() = irDynamicFlag<K>()
fun<K : IrElement> irInterceptedLocal_HashSet() = HashSet<K>()
fun<K : IrElement> irIntercepted_MutableSet() = irDynamicFlag<K>()
fun<K : IrElement> irIntercepted_ConcurrentHashSet() = irDynamicFlag<K>()
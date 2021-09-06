package com.luggsoft.pml.core.boxing

import kotlin.reflect.KCallable

fun <TResult> KCallable<TResult>.call(arguments: List<Any?>): Any? = this.call(
    args = arguments.toTypedArray()
)

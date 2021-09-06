package com.luggsoft.pml.core.boxing

import kotlin.reflect.KCallable

fun KCallable<*>.toBoxFunction(thisRef: Any? = null): BoxFunction = BoxFunction { argumentBoxes ->
    return@BoxFunction this@toBoxFunction
        .call(
            arguments = (if (thisRef == null) emptyList() else listOf(thisRef))
                .plus(argumentBoxes.map(Box<*>::value))
        )
        .toBox()
}


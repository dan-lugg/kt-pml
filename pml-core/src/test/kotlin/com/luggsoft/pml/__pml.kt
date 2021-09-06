package com.luggsoft.pml

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.random.Random

fun main()
{
    loadTestNode("/TestNode1.json").also(::println)
}

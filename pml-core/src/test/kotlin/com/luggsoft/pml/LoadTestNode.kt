package com.luggsoft.pml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.luggsoft.pml.models.TestNode

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun loadTestNode(name: String, objectMapper: ObjectMapper = jacksonObjectMapper()): TestNode = ::loadTestNode::class.java
    .getResourceAsStream(name)
    .let(objectMapper::readValue)

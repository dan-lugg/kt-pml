package com.luggsoft.pml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.luggsoft.pml.models.TestNode
import java.io.File
import java.io.Reader

class JsonTestNodeProvider(
    private val reader: Reader,
    private val objectMapper: ObjectMapper,
) : TestNodeProvider
{
    constructor(
        file: File,
        objectMapper: ObjectMapper,
    ) : this(
        reader = file.reader(),
        objectMapper = objectMapper,
    )

    override fun getTestNode(): TestNode = this.objectMapper.readValue(this.reader)
}


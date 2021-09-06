package com.luggsoft.pml.models

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_type")
interface TestNode
{
    val childTestNodes: List<TestNode>
}

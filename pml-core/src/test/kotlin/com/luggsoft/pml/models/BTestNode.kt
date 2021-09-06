package com.luggsoft.pml.models

import com.luggsoft.pml.PmlName

@PmlName("BTestNode")
data class BTestNode(
    override val childTestNodes: List<TestNode> = emptyList(),
) : TestNode

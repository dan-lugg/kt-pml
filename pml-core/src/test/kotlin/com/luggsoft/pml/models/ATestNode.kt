package com.luggsoft.pml.models

import com.luggsoft.pml.PmlName

@PmlName("ATestNode")
data class ATestNode(
    override val childTestNodes: List<TestNode> = emptyList(),
) : TestNode

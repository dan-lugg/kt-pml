package com.luggsoft.pml.models

import com.luggsoft.pml.PmlName

@PmlName("CTestNode")
data class CTestNode(
    override val childTestNodes: List<TestNode> = emptyList(),
) : TestNode

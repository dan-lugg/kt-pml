package com.luggsoft.pml

import com.luggsoft.pml.models.TestNode

val TestNode.depth: Int
    get() = 1 + this.childTestNodes.sumOf(TestNode::depth)

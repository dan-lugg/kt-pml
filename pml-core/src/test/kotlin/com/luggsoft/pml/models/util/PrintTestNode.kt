package com.luggsoft.pml

import com.luggsoft.pml.models.TestNode

fun printTestNode(testNode: TestNode, ancestorTestNodes: List<TestNode> = emptyList())
{
    println("${"\t".repeat(ancestorTestNodes.size)}${testNode::class.simpleName} (${testNode.depth})")
    testNode.childTestNodes.forEach { childTestNode ->
        printTestNode(childTestNode, ancestorTestNodes + testNode)
    }
}

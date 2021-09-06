package com.luggsoft.pml.models

interface TestNodeHandler<TTestNode : TestNode>
{
    fun handleTestNode(testNode: TTestNode)
}

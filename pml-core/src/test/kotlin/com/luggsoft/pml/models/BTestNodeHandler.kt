package com.luggsoft.pml.models

class BTestNodeHandler : TestNodeHandler<BTestNode>
{
    override fun handleTestNode(testNode: BTestNode) = println("${this::class.simpleName}(testNode=$testNode)")
}

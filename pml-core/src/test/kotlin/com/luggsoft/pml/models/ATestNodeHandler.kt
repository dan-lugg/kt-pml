package com.luggsoft.pml.models

class ATestNodeHandler : TestNodeHandler<ATestNode>
{
    override fun handleTestNode(testNode: ATestNode) = println("${this::class.simpleName}(testNode=$testNode)")
}

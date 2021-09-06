package com.luggsoft.pml.models

class CTestNodeHandler : TestNodeHandler<CTestNode>
{
    override fun handleTestNode(testNode: CTestNode) = println("${this::class.simpleName}(testNode=$testNode)")
}

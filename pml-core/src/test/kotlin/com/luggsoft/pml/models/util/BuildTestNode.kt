package com.luggsoft.pml

import com.luggsoft.pml.models.ATestNode
import com.luggsoft.pml.models.BTestNode
import com.luggsoft.pml.models.CTestNode
import com.luggsoft.pml.models.TestNode
import kotlin.random.Random
import kotlin.random.nextInt

fun buildTestNode(random: Random): TestNode
{
    fun buildTestNodes(random: Random, depth: Int): List<TestNode> = when (random.nextInt(0..depth))
    {
        0, 1 -> (0 until (random.nextInt(5) + 1)).map { i ->
            val childTestNodes = buildTestNodes(random, depth + 1)
            return@map listOf(::ATestNode, ::BTestNode, ::CTestNode)
                .random(random)
                .invoke(childTestNodes)
        }
        else -> emptyList()

    }

    while (true)
    {
        return buildTestNodes(random, 0).maxByOrNull(TestNode::depth)
            ?: continue
    }
}

package com.luggsoft.pml.core.ast

import com.github.h0tk3y.betterParse.lexer.TokenMatch

enum class Operator(
    val symbol: String,
)
{
    SCOPED("."),

    // <editor-fold desc="LOGICAL">

    LOGICAL_DISJUNCT("||"),
    LOGICAL_CONJUNCT("&&"),

    // </editor-fold>

    // <editor-fold desc="COMPARE_ABSOLUTE">

    COMPARE_ABSOLUTE_EQ("=="),
    COMPARE_ABSOLUTE_NEQ("!="),

    // </editor-fold>

    // <editor-fold desc="COMPARE_RELATIVE">

    COMPARE_RELATIVE_LT("<"),
    COMPARE_RELATIVE_GT(">"),
    COMPARE_RELATIVE_LTE("<="),
    COMPARE_RELATIVE_GTE(">="),

    // </editor-fold>

    // <editor-fold desc="COMPARE_SPECIAL">

    COMPARE_SPECIAL_LIKE("?=")

    // </editor-fold>

    ;

    companion object
    {
        fun fromSymbol(symbol: String): Operator = values().first { operator -> operator.symbol == symbol }

        fun fromTokenMatch(tokenMatch: TokenMatch): Operator = fromSymbol(symbol = tokenMatch.text)
    }
}

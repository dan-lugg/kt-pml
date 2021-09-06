package com.luggsoft.pml.core

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.RegexToken
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parser
import com.luggsoft.pml.Selector
import com.luggsoft.pml.core.ast.AccessMemberExpression
import com.luggsoft.pml.core.ast.DeepSelectorElement
import com.luggsoft.pml.core.ast.ElementMatcher
import com.luggsoft.pml.core.ast.Expression
import com.luggsoft.pml.core.ast.FlatSelectorElement
import com.luggsoft.pml.core.ast.InvokeMemberExpression
import com.luggsoft.pml.core.ast.MemberExpression
import com.luggsoft.pml.core.ast.NameElementMatcher
import com.luggsoft.pml.core.ast.Operator
import com.luggsoft.pml.core.ast.RootExpression
import com.luggsoft.pml.core.ast.ScopedExpression
import com.luggsoft.pml.core.ast.SelectorElement
import com.luggsoft.pml.core.ast.ValueExpression
import com.luggsoft.pml.core.ast.WildElementMatcher
import com.luggsoft.pml.core.boxing.DoubleBox
import com.luggsoft.pml.core.boxing.FloatBox
import com.luggsoft.pml.core.boxing.IntBox
import com.luggsoft.pml.core.boxing.LongBox
import com.luggsoft.pml.core.boxing.StringBox
import org.intellij.lang.annotations.Language

/**
 * # Selectors
 *
 * Selectors follow a simple syntax, as expressed below:
 * ```
 *     _____Element,_Matcher_____
 *    /                          \
 *
 * ( ( '>' | '~' ) ( <name> | '*' ) ( '{' <expression> '}' )? )+
 *
 *                                   \____________________/
 *                                          Optional
 *                                         Expression
 * ```
 *
 * ## Elements
 *
 * `>` The element should perform a "flat" search; as in, only the direct children of the parent
 *
 * `~` The element should perform a "deep" search; as in, all descendants of the parent
 *
 * ## Matchers
 *
 * `<name>` The matching is done on the provided name; as in, a named match
 *
 * `*` The matching is done on all names; as in, a wildcard match
 *
 * # Expressions
 *
 * Expressions use a syntax similar to most C-like languages; probably most like Groovy or Kotlin.
 * Expressions also follow similar operator associativity and precedence rules.
 *
 * `$` Refers to "this"; as in, the current context object
 *
 */
class SelectorGrammar : Grammar<Selector>()
{
    // <editor-fold desc="Tokens">

    val spaceToken by expressionToken(pattern = "\\s*", ignore = true)

    val commentToken by expressionToken(pattern = "/\\*.+?\\*/", ignore = true)

    val commaToken by literalToken(text = ",")

    val flatToken by literalToken(text = ">")

    val deepToken by literalToken(text = "~")

    val wildToken by literalToken(text = "*")

    val rootToken by literalToken(text = "$")

    val lParenToken by literalToken(text = "(")

    val rParenToken by literalToken(text = ")")

    val lBraceToken by literalToken(text = "{")

    val rBraceToken by literalToken(text = "}")

    val nullToken by literalToken(text = "null")

    val intOrLongNumberToken by expressionToken(pattern = "[+-]?([1-9][0-9]*|0)[il]")

    val floatOrDoubleNumberToken by expressionToken(pattern = "[+-]?([1-9][0-9]*|0)\\.[0-9]+[fd]")

    val numberToken by or(
        intOrLongNumberToken,
        floatOrDoubleNumberToken,
    )

    val stringToken by expressionToken(pattern = "'((\\\\'|[^'])*)'")

    val trueBooleanToken by literalToken(text = "true")

    val falseBooleanToken by literalToken(text = "false")

    val identifierToken by expressionToken(pattern = "[a-zA-Z_][a-zA-Z0-9_]*")

    val scopedOperatorToken by literalToken(text = Operator.SCOPED.symbol)

    val compareSpecialOperatorToken by literalToken(text = Operator.COMPARE_SPECIAL_LIKE.symbol)

    val compareRelativeLteOperatorToken by literalToken(text = Operator.COMPARE_RELATIVE_LTE.symbol)

    val compareRelativeGteOperatorToken by literalToken(text = Operator.COMPARE_RELATIVE_GTE.symbol)

    val compareRelativeLtOperatorToken by literalToken(text = Operator.COMPARE_RELATIVE_LT.symbol)

    val compareRelativeGtOperatorToken by literalToken(text = Operator.COMPARE_RELATIVE_GT.symbol)

    val compareAbsoluteEqOperatorToken by literalToken(text = Operator.COMPARE_ABSOLUTE_EQ.symbol)

    val compareAbsoluteNeqOperatorToken by literalToken(text = Operator.COMPARE_ABSOLUTE_NEQ.symbol)

    val logicalConjunctOperatorToken by literalToken(text = Operator.LOGICAL_CONJUNCT.symbol)

    val logicalDisjunctOperatorToken by literalToken(text = Operator.LOGICAL_DISJUNCT.symbol)

    val compareRelativeOperatorToken by or(
        compareRelativeLteOperatorToken,
        compareRelativeGteOperatorToken,
        compareRelativeLtOperatorToken,
        compareRelativeGtOperatorToken,
    )

    val compareAbsoluteOperatorToken by or(
        compareAbsoluteEqOperatorToken,
        compareAbsoluteNeqOperatorToken,
    )

    // </editor-fold>

    // <editor-fold desc="Expression Parsers">

    val rootExpressionParser: Parser<RootExpression> by rootToken asJust RootExpression

    val numberValueExpressionParser: Parser<ValueExpression> by numberToken map expression@{ tokenMatch ->
        return@expression ValueExpression(
            valueBox = when (val type = tokenMatch.text.takeLast(1))
            {
                "i" -> IntBox(value = tokenMatch.text.dropLast(1).toInt())
                "l" -> LongBox(value = tokenMatch.text.dropLast(1).toLong())
                "f" -> FloatBox(value = tokenMatch.text.dropLast(1).toFloat())
                "d" -> DoubleBox(value = tokenMatch.text.dropLast(1).toDouble())
                else -> throw NumberFormatException("Unexpected type suffix, '$type'")
            }
        )
    }

    val stringValueExpressionParser: Parser<ValueExpression> by stringToken map expression@{ tokenMatch ->
        return@expression ValueExpression(
            valueBox = StringBox(
                value = tokenMatch.text.substring(1, tokenMatch.text.length - 1),
            ),
        )
    }

    val nullValueExpressionParser: Parser<ValueExpression> by nullToken asJust ValueExpression.Null

    val trueBooleanValueExpressionParser: Parser<ValueExpression> by trueBooleanToken asJust ValueExpression.True

    val falseBooleanValueExpressionParser: Parser<ValueExpression> by falseBooleanToken asJust ValueExpression.False

    val valueExpressionParser: Parser<ValueExpression> by or(
        nullValueExpressionParser,
        numberValueExpressionParser,
        stringValueExpressionParser,
        trueBooleanValueExpressionParser,
        falseBooleanValueExpressionParser,
    )

    val accessMemberExpressionParser: Parser<AccessMemberExpression> by identifierToken map expression@{ tokenMatch ->
        return@expression AccessMemberExpression(
            name = tokenMatch.text,
        )
    }

    val invokeMemberExpressionParser: Parser<InvokeMemberExpression> by identifierToken and skip(lParenToken) and separatedTerms(parser(::logicalDisjunctExpressionParser), commaToken, true) and skip(rParenToken) map expression@{ (tokenMatch, argumentExpressions) ->
        return@expression InvokeMemberExpression(name = tokenMatch.text, argumentExpressions = argumentExpressions)
    }

    val memberExpressionParser: Parser<MemberExpression> by or(
        invokeMemberExpressionParser,
        accessMemberExpressionParser,
    )

    val expressionParser: Parser<Expression> by or(
        rootExpressionParser,
        valueExpressionParser,
        memberExpressionParser,
        skip(lParenToken) and parser(::logicalDisjunctExpressionParser) and skip(rParenToken),
    )

    val scopedExpressionParser: Parser<Expression> by leftAssociative(expressionParser, scopedOperatorToken) expression@{ leftExpression, _, rightExpression ->
        return@expression ScopedExpression(
            expression = leftExpression, memberExpression = when (rightExpression)
            {
                is MemberExpression -> rightExpression
                else -> throw ParseException(
                    errorResult = TreeErrorResult(
                        message = "Expected ${MemberExpression::class.simpleName}, instead received ${rightExpression::class.simpleName}",
                    )
                )
            }
        )
    }

    val compareSpecialOperatorParser: Parser<Operator> by compareSpecialOperatorToken map Operator.Companion::fromTokenMatch

    val compareSpecialExpressionParser: Parser<Expression> by leftAssociative(scopedExpressionParser, compareSpecialOperatorParser, ::leftAssociativeToInvokeMemberExpression)

    val compareRelativeOperatorParser: Parser<Operator> by compareRelativeOperatorToken map Operator.Companion::fromTokenMatch

    val compareRelativeExpressionParser: Parser<Expression> by leftAssociative(compareSpecialExpressionParser, compareRelativeOperatorParser, ::leftAssociativeToInvokeMemberExpression)

    val compareAbsoluteOperatorParser: Parser<Operator> by compareAbsoluteOperatorToken map Operator.Companion::fromTokenMatch

    val compareAbsoluteExpressionParser: Parser<Expression> by leftAssociative(compareRelativeExpressionParser, compareAbsoluteOperatorParser, ::leftAssociativeToInvokeMemberExpression)

    val logicalConjunctOperatorParser: Parser<Operator> by logicalConjunctOperatorToken map Operator.Companion::fromTokenMatch

    val logicalConjunctExpressionParser: Parser<Expression> by leftAssociative(compareAbsoluteExpressionParser, logicalConjunctOperatorParser, ::leftAssociativeToInvokeMemberExpression)

    val logicalDisjunctOperatorParser: Parser<Operator> by logicalDisjunctOperatorToken map Operator.Companion::fromTokenMatch

    val logicalDisjunctExpressionParser: Parser<Expression> by leftAssociative(logicalConjunctExpressionParser, logicalDisjunctOperatorParser, ::leftAssociativeToInvokeMemberExpression)

    // </editor-fold>

    // <editor-fold desc="Selector Parsers">

    val nameElementMatcherParser: Parser<NameElementMatcher> by identifierToken and optional(skip(lBraceToken) and logicalDisjunctExpressionParser and skip(rBraceToken)) map matcher@{ (tokenMatch, expression) ->
        return@matcher NameElementMatcher(
            name = tokenMatch.text,
            expression = expression,
        )
    }

    val wildElementMatcherParser: Parser<WildElementMatcher> by skip(wildToken) and optional(skip(lBraceToken) and logicalDisjunctExpressionParser and skip(rBraceToken)) map matcher@{ expression ->
        return@matcher WildElementMatcher(
            expression = expression,
        )
    }

    val elementMatcherParser: Parser<ElementMatcher> by or(
        nameElementMatcherParser,
        wildElementMatcherParser,
    )

    val flatSelectorElementParser: Parser<FlatSelectorElement> by skip(flatToken) and elementMatcherParser map element@{ elementMatcher ->
        return@element FlatSelectorElement(
            elementMatcher = elementMatcher,
        )
    }

    val deepSelectorElementParser: Parser<DeepSelectorElement> by skip(deepToken) and elementMatcherParser map element@{ elementMatcher ->
        return@element DeepSelectorElement(
            elementMatcher = elementMatcher,
        )
    }

    val selectorElementParser: Parser<SelectorElement> by or(
        flatSelectorElementParser,
        deepSelectorElementParser,
    )

    val selectorParser: Parser<Selector> by oneOrMore(selectorElementParser) map selector@{ selectorElements ->
        return@selector DefaultSelector(
            selectorElements = selectorElements,
        )
    }

    override val rootParser: Parser<Selector> by selectorParser

    // </editor-fold>

    private companion object
    {
        fun leftAssociativeToInvokeMemberExpression(leftExpression: Expression, operator: Operator, rightExpression: Expression): InvokeMemberExpression = InvokeMemberExpression(
            name = operator.name, argumentExpressions = listOf(
                leftExpression,
                rightExpression,
            )
        )

        fun expressionToken(@Language("RegExp") pattern: String, ignore: Boolean = false): RegexToken = regexToken(
            pattern = pattern,
            ignore = ignore,
        )

        fun <T> or(vararg parsers: Parser<T>): Parser<T> = parsers.reduce { orParser, parser -> orParser or parser }
    }
}

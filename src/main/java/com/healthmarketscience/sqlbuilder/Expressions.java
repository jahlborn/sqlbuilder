/*
Copyright (c) 2020 James Ahlborn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.healthmarketscience.sqlbuilder;

import com.healthmarketscience.sqlbuilder.custom.NamedParamObject;

/**
 * Useful static accessors for building expressions.
 *
 * @author James Ahlborn
 */
public class Expressions
{
  private Expressions() {}

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '+'.
   */
  public static ComboExpression add() {
    return new ComboExpression(ComboExpression.Op.ADD);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '+'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression add(Object... expressions) {
    return new ComboExpression(ComboExpression.Op.ADD, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '-'.
   */
  public static ComboExpression subtract() {
    return new ComboExpression(ComboExpression.Op.SUBTRACT);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '-'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression subtract(Object... expressions) {
    return new ComboExpression(ComboExpression.Op.SUBTRACT, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '*'.
   */
  public static ComboExpression multiply() {
    return new ComboExpression(ComboExpression.Op.MULTIPLY);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '*'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression multiply(Object... expressions) {
    return new ComboExpression(ComboExpression.Op.MULTIPLY, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '/'.
   */
  public static ComboExpression divide() {
    return new ComboExpression(ComboExpression.Op.DIVIDE);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '/'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression divide(Object... expressions) {
    return new ComboExpression(ComboExpression.Op.DIVIDE, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '||'.
   */
  public static ComboExpression concatenate() {
    return new ComboExpression(ComboExpression.Op.CONCATENATE);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '||'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression concatenate(Object... expressions) {
    return new ComboExpression(ComboExpression.Op.CONCATENATE, expressions);
  }

  /**
   * Convenience method for generating the negation of an expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toExpressionObject(Object)}.
   */
  public static NegateExpression negate(Object obj) {
    return new NegateExpression(obj);
  }

  /**
   * Convenience method for generating an EXTRACT year expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractYear(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.YEAR, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT month expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractMonth(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.MONTH, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT day expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractDay(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.DAY, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT hour expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractHour(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.HOUR, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT minute expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractMinute(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.MINUTE, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT second expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractSecond(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.SECOND, dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT timezone hour expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractTimezoneHour(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.TIMEZONE_HOUR,
                                 dateExpr);
  }

  /**
   * Convenience method for generating an EXTRACT timezone minute expression.
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static ExtractExpression extractTimezoneMinute(Object dateExpr) {
    return new ExtractExpression(ExtractExpression.DatePart.TIMEZONE_MINUTE,
                                 dateExpr);
  }

  /**
   * Convenience method for generating a CASE statement.
   */
  public static CaseStatement caseStmt() {
    return new CaseStatement();
  }

  /**
   * Convenience method for generating a simple CASE statement.
   */
  public static SimpleCaseStatement caseStmt(Object columnObj) {
    return new SimpleCaseStatement(columnObj);
  }

  /**
   * Convenience method for generating a subquery expression.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public static Subquery subquery(Object query) {
    return new Subquery(query);
  }

  /**
   * Convenience method for generating a custom "named" parameter expression.
   */
  public static NamedParamObject namedParam(String name) {
    return new NamedParamObject(name);
  }

  /**
   * Convenience method for generating a common table expression.
   */
  public static CommonTableExpression cte(String name) {
    return new CommonTableExpression(name);
  }

  /**
   * Convenience method for generating a custom expression.
   */
  public static CustomExpression customExpr(Object exprObj) {
    return new CustomExpression(exprObj);
  }

  /** an Expression object which will always return <code>true</code> for
      {@link Expression#isEmpty}.  useful for selectively including expression
      blocks */
  public static Expression emptyExpr() {
    return Expression.EMPTY;
  }

  /**
   * Convenience method for generating literal sql (not truly an "expression",
   * but convenient nonetheless).
   */
  public static CustomSql customSql(Object obj) {
    return new CustomSql(obj);
  }
}

/*
Copyright (c) 2008 Health Market Science, Inc.

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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.healthmarketscience.common.util.AppendableExt;



/**
 * Outputs a binary midfix based condition
 * <code>"(&lt;column1&gt; &lt;binaryOp&gt; &lt;column2&gt;)"</code>.
 *
 * @author James Ahlborn
 */
public class BinaryCondition extends Condition
{
  /**
   * Enum representing the binary midfix operations supported in a SQL
   * condition, e.g.
   * <code>"(&lt;column1&gt; &lt;binaryOp&gt; &lt;column2&gt;)"</code>.
   */
  public enum Op
  {
    LESS_THAN(" < "),
    LESS_THAN_OR_EQUAL_TO(" <= "),
    GREATER_THAN(" > "),
    GREATER_THAN_OR_EQUAL_TO(" >= "),
    EQUAL_TO(" = "),
    NOT_EQUAL_TO(" <> "),
    LIKE(" LIKE "),
    NOT_LIKE(" NOT LIKE ");

    private final String _opStr;

    private Op(String opStr) {
      _opStr = opStr;
    }

    @Override
    public String toString() { return _opStr; }
  }


  private Object _binaryOp;
  private SqlObject _leftValue;
  private SqlObject _rightValue;
  private ValueObject _escapeChar;

  public BinaryCondition(Op binaryOp,
                         SqlObject leftValue,
                         SqlObject rightValue) {
    this(binaryOp, (Object)leftValue, (Object)rightValue);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public BinaryCondition(Op binaryOp,
                         Object leftValue,
                         Object rightValue) {
    this((Object)binaryOp, leftValue, rightValue);
  }
    
  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public BinaryCondition(Object binaryOpStr,
                         Object leftValue,
                         Object rightValue) {
    _binaryOp = binaryOpStr;
    _leftValue = Converter.toColumnSqlObject(leftValue);
    _rightValue = Converter.toColumnSqlObject(rightValue);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _leftValue.collectSchemaObjects(vContext);
    _rightValue.collectSchemaObjects(vContext);
  }

  protected boolean supportsEscape(Object binaryOp) {
    return ((binaryOp == Op.LIKE) || (binaryOp == Op.NOT_LIKE));
  }

  /**
   * Sets the escape charactor for a [NOT] LIKE condition pattern.
   */
  public BinaryCondition setLikeEscapeChar(Character escapeChar) {
    if(!supportsEscape(_binaryOp)) {
      throw new IllegalArgumentException(
          "Escape char is not valid for '" + _binaryOp + "' operator");
    }

    _escapeChar = ((escapeChar != null) ?
                   (new ValueObject(escapeChar)) : null);
    return this;
  }
  
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    openParen(app);
    app.append(_leftValue).append(_binaryOp).append(_rightValue);
    if(_escapeChar != null) {
      app.append(" ESCAPE ").append(_escapeChar);
    }
    closeParen(app);
  }

  /**
   * Escapes the special chars '%', '_', and the given char itself in the
   * given literal string using the given escape character.
   * 
   * @param literal string to escape as a literal pattern
   * @param escapeChar escape character to use to escape the literal
   * @return the escaped string
   */
  public static String escapeLikeLiteral(String literal, char escapeChar) {
    // escape instances of the escape char, '%' or '_'
    String escapeStr = String.valueOf(escapeChar);
    return literal.replaceAll("([%_" + Pattern.quote(escapeStr) + "])",
                              Matcher.quoteReplacement(escapeStr) + "$1");
  }
  
  /**
   * Convenience method for generating a Condition for testing if a column
   * is less than another column (inclusive or exclusive).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThan(Object value1, Object value2,
                                         boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                Op.LESS_THAN_OR_EQUAL_TO :
                                Op.LESS_THAN),
                               value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is strictly less than another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThan(Object value1, Object value2) {
    return new BinaryCondition(Op.LESS_THAN, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is less than or equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThanOrEq(Object value1, Object value2) {
    return new BinaryCondition(Op.LESS_THAN_OR_EQUAL_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is greater than a given value (inclusive or exclusive).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThan(Object value1, Object value2,
                                            boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                Op.GREATER_THAN_OR_EQUAL_TO :
                                Op.GREATER_THAN),
                               value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is strictly greater than a given value.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThan(Object value1, Object value2) {
    return new BinaryCondition(Op.GREATER_THAN, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is greater than or equal to a given value.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThanOrEq(Object value1, Object value2) {
    return new BinaryCondition(Op.GREATER_THAN_OR_EQUAL_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition equalTo(Object value1, Object value2) {
    return new BinaryCondition(Op.EQUAL_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notEqualTo(Object value1, Object value2) {
    return new BinaryCondition(Op.NOT_EQUAL_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition like(Object value1, Object value2) {
    return new BinaryCondition(Op.LIKE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notLike(Object value1, Object value2) {
    return new BinaryCondition(Op.NOT_LIKE, value1, value2);
  }
    
}

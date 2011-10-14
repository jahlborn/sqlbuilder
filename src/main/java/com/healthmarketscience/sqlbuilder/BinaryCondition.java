/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
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

  /**
   * Sets the escape charactor for a [NOT] LIKE condition pattern.
   */
  public BinaryCondition setLikeEscapeChar(Character escapeChar)
  {
    if((_binaryOp != Op.LIKE) && (_binaryOp != Op.NOT_LIKE)) {
      throw new IllegalArgumentException(
          "Escape char only valid for [NOT] LIKE");
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
  public static String escapeLikeLiteral(String literal, char escapeChar)
  {
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
  public static BinaryCondition lessThan(Object value1,
                                         Object value2,
                                         boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                Op.LESS_THAN_OR_EQUAL_TO :
                                Op.LESS_THAN),
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is greater than a given value (inclusive or exclusive).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThan(Object value1,
                                            Object value2,
                                            boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                Op.GREATER_THAN_OR_EQUAL_TO :
                                Op.GREATER_THAN),
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition equalTo(Object value1,
                                        Object value2) {
    return new BinaryCondition(Op.EQUAL_TO,
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notEqualTo(Object value1,
                                           Object value2) {
    return new BinaryCondition(Op.NOT_EQUAL_TO,
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition like(Object value1,
                                     Object value2) {
    return new BinaryCondition(Op.LIKE,
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notLike(Object value1,
                                        Object value2) {
    return new BinaryCondition(Op.NOT_LIKE,
                               Converter.toColumnSqlObject(value1),
                               Converter.toColumnSqlObject(value2));
  }
    
}

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

import com.healthmarketscience.sqlbuilder.dbspec.Column;

/**
 * Outputs a simple case statement like:
 * <code>"CASE &lt;column1&gt; WHEN &lt;val1&gt; THEN &lt;result1&gt; [ WHEN
 * &lt;val2&gt; THEN &lt;result2&gt; WHEN ... ] [ELSE &lt;resultN&gt;]
 * END"</code> (where the values are numeric/string values).
 *
 * @author James Ahlborn
 */
public class SimpleCaseStatement extends BaseCaseStatement<SimpleCaseStatement>
{

  /**
   * @param column the column to reference at the beginning of the "CASE"
   *               statement
   */
  public SimpleCaseStatement(Column column) {
    this((Object)column);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   * 
   * @param operand the custom column to reference at the beginning of the
   *                "CASE" statement
   */
  public SimpleCaseStatement(Object operand) {
    super(Converter.toColumnSqlObject(operand));
  }

  /**
   * Adds a "WHEN" clause to the "CASE" statement.
   * <p>
   * Value {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toValueSqlObject(Object)}.
   * Result {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   *
   * @param value the value to test against the column of this "CASE"
   *              statement
   * @param result the result to output if this "WHEN" clause is selected
   */
  public SimpleCaseStatement addNumericWhen(Object value, Object result) {
    return addCustomWhen(Converter.toValueSqlObject(value), result);
  }
  
  /**
   * Adds a "WHEN" clause to the "CASE" statement.
   * <p>
   * Result {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   *
   * @param value the value to test against the column of this "CASE"
   *              statement
   * @param result the result to output if this "WHEN" clause is selected
   */
  public SimpleCaseStatement addWhen(String value, Object result) {
    return addCustomWhen(new ValueObject(value), result);
  }
  
}

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

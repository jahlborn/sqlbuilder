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

/**
 * Outputs a case statement like:
 * <code>"CASE WHEN &lt;cond1&gt; THEN &lt;result1&gt; [ WHEN
 * &lt;cond2&gt; THEN &lt;result2&gt; WHEN ... ] [ELSE &lt;resultN&gt;]
 * END"</code>
 *
 * @author James Ahlborn
 */
public class CaseStatement extends BaseCaseStatement<CaseStatement>
{
  public CaseStatement() {
    super(null);
  }

  /**
   * Adds a "WHEN" clause to the "CASE" statement.
   * <p>
   * Result {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   *
   * @param test the condition to test for this "WHEN" clause
   * @param result the result to output if this "WHEN" clause is selected
   */
  public CaseStatement addWhen(Condition test, Object result) {
    return addCustomWhen(test, result);
  }
  
}

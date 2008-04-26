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

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
 * Outputs a JDBC escaped scalar function call
 * <code>"{fn &lt;funcCall&gt;}"</code>.
 *
 * @author James Ahlborn
 */
public class JdbcScalarFunction extends JdbcEscape {

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public JdbcScalarFunction(Object functionCall) {
    super(Type.SCALAR_FUNCTION,
          Converter.toCustomSqlObject(functionCall));
  }

  /** JdbcScalarFunction which represents the scalar function NOW for
      returning a timestamp of the current time. */
  public static final JdbcScalarFunction NOW =
    new JdbcScalarFunction(new FunctionCall(new CustomSql("NOW")));
  
}

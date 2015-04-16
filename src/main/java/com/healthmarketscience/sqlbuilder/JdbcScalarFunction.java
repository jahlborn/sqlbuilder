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

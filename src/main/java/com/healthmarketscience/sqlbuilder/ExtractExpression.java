/*
Copyright (c) 2017 Andrey Karepin

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
import com.healthmarketscience.common.util.AppendableExt;


/**
 * Outputs an extract expression like:
 * <code>"EXTRACT(&lt;datePart&gt; FROM &lt;dateExpression&gt;)"</code>
 *
 * @see "SQL 2003"
 * @author Andrey Karepin
 */
public class ExtractExpression extends Expression
{
  /**
   * The SQL defined date parts for the extract expression.  Many databases
   * have extensions to these choices which can be found in the relevant
   * extensions module.
   * 
   * @see com.healthmarketscience.sqlbuilder.custom.postgresql.PgExtractDatePart
   * @see com.healthmarketscience.sqlbuilder.custom.mysql.MysExtractDatePart
   * @see com.healthmarketscience.sqlbuilder.custom.oracle.OraExtractDatePart
   */
  public enum DatePart
  {
    YEAR,
    MONTH, 
    DAY, 
    HOUR, 
    MINUTE, 
    SECOND, 
    TIMEZONE_HOUR, 
    TIMEZONE_MINUTE;
  }

  private final Object _datePart;
  private final SqlObject _dateExpression;

  public ExtractExpression(DatePart datePart, Object dateExpression) {
    this((Object)datePart, dateExpression);
  }

  public ExtractExpression(Object datePart, Object dateExpression) {
    _datePart = datePart;
    _dateExpression = Converter.toColumnSqlObject(dateExpression);
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _dateExpression.collectSchemaObjects(vContext);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append("EXTRACT(")
      .append(_datePart)
      .append(" FROM ")
      .append(_dateExpression)
      .append(")");
  }
}

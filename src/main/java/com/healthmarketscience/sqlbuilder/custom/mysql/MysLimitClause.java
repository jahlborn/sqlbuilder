/*
Copyright (c) 2015 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom.mysql;

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.Converter;
import com.healthmarketscience.sqlbuilder.NumberValueObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.ValidationException;
import com.healthmarketscience.sqlbuilder.Verifiable;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;


/**
 * Appends a MySQL limit clause like {@code " LIMIT [<offset>] <limit>"} for
 * use in {@link SelectQuery}s.
 *
 * @see SelectQuery#addCustomization(CustomSyntax)
 * 
 * @author James Ahlborn
 */
public class MysLimitClause extends CustomSyntax 
  implements Verifiable<MysLimitClause>
{
  private SqlObject _rowCount;
  private SqlObject _offset;

  public MysLimitClause(Object rowCount) {
    this(null, rowCount);
  }

  public MysLimitClause(Object offset, Object rowCount) {
    _offset = ((offset != null) ? Converter.toValueSqlObject(offset) : null);
    _rowCount = Converter.toValueSqlObject(rowCount);
  }

  @Override
  public void apply(SelectQuery query) {
    query.addCustomization(SelectQuery.Hook.FOR_UPDATE, HookType.BEFORE, this);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(" LIMIT ");
    if(_offset != null) {
      app.append(_offset).append(", ");
    }
    app.append(_rowCount);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    vContext.addVerifiable(this);
    if(_offset != null) {
      collectSchemaObjects(_offset, vContext);
    }
    collectSchemaObjects(_rowCount, vContext);
  }

  @Override
  public final MysLimitClause validate() throws ValidationException {
    doValidate();
    return this;
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    if(_offset != null) {
      validateValue(_offset, "offset");
    }
    if(_rowCount == null) {
      throw new ValidationException("Limit clause is missing row count");
    }
    validateValue(_rowCount, "row count");
  }

  private static void validateValue(SqlObject valueObj, String type) {
    if(!(valueObj instanceof NumberValueObject)) {
      // nothing we can do, custom value
      return;
    }
    if(!((NumberValueObject)valueObj).isIntegralInRange(0, Long.MAX_VALUE)) {
        throw new ValidationException(
          "Limit " + type + " value must be positive integer, given: " + 
          valueObj);
    }
  }
}

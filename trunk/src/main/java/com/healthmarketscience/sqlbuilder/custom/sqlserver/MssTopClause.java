/*
Copyright (c) 2016 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom.sqlserver;

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.Converter;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;

/**
 * Appends a SQLServer TOP clause like {@code " TOP <count> [PERCENT]"} for
 * use in {@link SelectQuery}s.
 *
 * @see SelectQuery#addCustomization(CustomSyntax)
 * 
 * @author James Ahlborn
 */
public class MssTopClause extends CustomSyntax
{
  private SqlObject _count;
  private boolean _isPercent;

  public MssTopClause(Object count) {
    this(count, false);
  }

  public MssTopClause(Object count, boolean isPercent) {
    _count = Converter.toValueSqlObject(count);
    _isPercent = isPercent;
  }

  public MssTopClause setIsPercent(boolean isPercent) {
    _isPercent = isPercent;
    return this;
  }

  @Override
  public void apply(SelectQuery query) {
    query.addCustomization(SelectQuery.Hook.DISTINCT, HookType.AFTER, this);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append("TOP ").append(_count).append(" ");
    if(_isPercent) {
      app.append("PERCENT ");
    }
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    collectSchemaObjects(_count, vContext);
  }
}

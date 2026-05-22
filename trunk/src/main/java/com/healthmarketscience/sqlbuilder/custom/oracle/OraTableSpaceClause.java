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

package com.healthmarketscience.sqlbuilder.custom.oracle;

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.CreateIndexQuery;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;

/**
 * Appends an Oracle {@code " TABLESPACE ..."} clause to a {@link
 * CreateTableQuery} or {@link CreateIndexQuery} if a tableSpace has been
 * specified.
 * 
 * @see CreateTableQuery#addCustomization(CustomSyntax)
 * @see CreateIndexQuery#addCustomization(CustomSyntax)
 *
 * @author James Ahlborn
 */
public class OraTableSpaceClause extends CustomSyntax
{
  private String _tableSpace;

  public OraTableSpaceClause(String tableSpace) {
    _tableSpace = tableSpace;
  }

  @Override
  public void apply(CreateTableQuery query) {
    query.addCustomization(CreateTableQuery.Hook.TRAILER, HookType.BEFORE, this);
  }

  @Override
  public void apply(CreateIndexQuery query) {
    query.addCustomization(CreateIndexQuery.Hook.TRAILER, HookType.BEFORE, this);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    if (_tableSpace != null) {
      app.append(" TABLESPACE " + _tableSpace);
    }
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    // nothing to do
  }
}

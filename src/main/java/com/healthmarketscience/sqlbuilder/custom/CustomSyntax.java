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

package com.healthmarketscience.sqlbuilder.custom;

import com.healthmarketscience.sqlbuilder.CreateIndexQuery;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SqlObject;

/**
 * Utility base class for custom SQL syntax instances.  Uses the visitor
 * pattern to enable custom instances to add themselves to the appropriate
 * locations in specific queries.  Subclasses only need to implement support
 * for queries to which they apply.
 * <p>
 * See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
 * custom SQL syntax.
 *
 * @author James Ahlborn
 */
public abstract class CustomSyntax extends SqlObject
{

  /**
   * Called by {@link SelectQuery#addCustomization(CustomSyntax)} to add this
   * custom syntax to the appropriate location in the SelectQuery.
   */
  public void apply(SelectQuery query) {
    throw new UnsupportedOperationException();
  }

  /**
   * Called by {@link CreateTableQuery#addCustomization(CustomSyntax)} to add this
   * custom syntax to the appropriate location in the CreateTableQuery.
   */
  public void apply(CreateTableQuery query) {
    throw new UnsupportedOperationException();
  }

  /**
   * Called by {@link CreateIndexQuery#addCustomization(CustomSyntax)} to add this
   * custom syntax to the appropriate location in the CreateIndexQuery.
   */
  public void apply(CreateIndexQuery query) {
    throw new UnsupportedOperationException();
  }
}

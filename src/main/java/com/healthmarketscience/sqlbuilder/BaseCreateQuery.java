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
 * Query which generates a CREATE statement.
 *
 * @author James Ahlborn
 */
public abstract class BaseCreateQuery<ThisType extends BaseCreateQuery<ThisType>>
  extends Query<ThisType>
{

  protected SqlObject _object;
  protected SqlObjectList<SqlObject> _columns = SqlObjectList.create();

  protected BaseCreateQuery(SqlObject objectStr) {
    _object = objectStr;
  }

  /**
   * Sets the name of the object being created.
   */
  public ThisType setName(String name) {
    return setCustomName(name);
  }

  /**
   * Sets the name of the object being created.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public ThisType setCustomName(Object name) {
    _object = Converter.toCustomSqlObject(name);
    return getThisType();
  }

  /** Adds column descriptions for the given Columns. */
  public ThisType addColumns(Column... columns) {
    return addCustomColumns((Object[])columns);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _object.collectSchemaObjects(vContext);
    _columns.collectSchemaObjects(vContext);
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // validate super
    super.validate(vContext);

    // can not have a "*" in the column list
    if(SelectQuery.hasAllColumns(_columns)) {
      throw new ValidationException("Cannot use the '*' syntax in this query");
    }
  }

  /** Adds the given SqlObjects as column descriptions, according to the
      subclass type. */
  public abstract ThisType addCustomColumns(Object... typedColumnStrs);
  
  /**
   * @return a DropQuery for the object which would be created by this create
   *         query.
   */
  public abstract DropQuery getDropQuery();  
  
}

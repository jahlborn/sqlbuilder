/*
Copyright (c) 2011 James Ahlborn

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
import java.util.List;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.ForeignKeyConstraint;

/**
 * Outputs a table or column foreign constraint clause (depending on the
 * current context)
 * <code>[ CONSTRAINT &lt;name&gt; ] FOREIGN KEY [ (&lt;col1&gt; ...) ] REFERENCES &lt;refRable&gt; [ (&lt;refCol1&gt; ...) ]</code>.
 *
 * @author James Ahlborn
 */
public class ForeignKeyConstraintClause extends ConstraintClause
{
  /**
   * Enum representing the referential action to be taken on update/delete.
   */
  public enum ReferentialAction
  {
    CASCADE("CASCADE"),
    /** @See "SQL 99" */
    RESTRICT("RESTRICT"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT"),
    NO_ACTION("NO ACTION");

    private final String _str;

    private ReferentialAction(String str) {
      _str = str;
    }

    @Override
    public String toString() {
      return _str;
    }
  }


  protected SqlObject _refTable;
  protected SqlObjectList<SqlObject> _refColumns = SqlObjectList.create();
  protected Object _onDeleteAction;
  protected Object _onUpdateAction;

  public ForeignKeyConstraintClause(ForeignKeyConstraint fkConstraint) {
    this(fkConstraint, fkConstraint.getColumns(),
         fkConstraint.getReferencedTable(),
         fkConstraint.getReferencedColumns());
  }

  public ForeignKeyConstraintClause(Object name, Object refTableStr) {
    this(name, null, refTableStr, null);
  }

  protected ForeignKeyConstraintClause(Object name, List<?> columns,
                                       Object refTableStr,
                                       List<?> refColumnStrs) {
    super(Type.FOREIGN_KEY, name, columns);

    _refTable = Converter.toCustomTableSqlObject(refTableStr);
    _refColumns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, refColumnStrs);
  }

  @Override
  public ForeignKeyConstraintClause addColumns(Column... columns) {
    return addCustomColumns((Object[])columns);
  }

  @Override
  public ForeignKeyConstraintClause addCustomColumns(Object... columnStrs) {
    super.addCustomColumns(columnStrs);
    return this;
  }

  /**
   * Adds a referenced column to the foreign key constraint definition.
   */
  public ForeignKeyConstraintClause addRefColumns(Column... columns) {
    return addCustomRefColumns((Object[])columns);
  }

  /**
   * Adds a custom referenced column to the foreign key constraint definition.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public ForeignKeyConstraintClause addCustomRefColumns(Object... columnStrs) {
    _refColumns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /**
   * Sets the referential action to be taken on delete.
   *
   * @see ReferentialAction
   */
  public ForeignKeyConstraintClause setOnDeleteAction(Object action) {
    _onDeleteAction = action;
    return this;
  }

  /**
   * Sets the referential action to be taken on update.
   *
   * @see ReferentialAction
   */
  public ForeignKeyConstraintClause setOnUpdateAction(Object action) {
    _onUpdateAction = action;
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);

    if(!vContext.isLocalOnly()) {
      // treat referenced objects as separate query objects
      ValidationContext refVContext = new ValidationContext(vContext);
      _refTable.collectSchemaObjects(refVContext);
      _refColumns.collectSchemaObjects(refVContext);
    }
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    preAppendTo(app);

    if(SqlContext.getContext(app).getUseTableConstraints()) {
      app.append(" ").append(_type.toString(false));
    }
    app.append(" ").append(_refTable);
    if(!_refColumns.isEmpty()) {
      app.append(" (").append(_refColumns).append(")");
    }

    if(_onDeleteAction != null) {
      app.append(" ON DELETE ").append(_onDeleteAction);
    }
    if(_onUpdateAction != null) {
      app.append(" ON UPDATE ").append(_onUpdateAction);
    }

    postAppendTo(app);
  }
}

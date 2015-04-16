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

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Query which generates an {@code ALTER TABLE} statement.
 *
 * @author James Ahlborn
 */
public class AlterTableQuery extends Query<AlterTableQuery>
{
  private SqlObject _table;
  private SqlObject _action;
  
  public AlterTableQuery(Table table) {
    this((Object)table);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public AlterTableQuery(Object tableStr) {
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /**
   * Sets the alter table action.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public AlterTableQuery setAction(Object action) {
    _action = Converter.toCustomSqlObject(action);
    return this;
  }

  /**
   * Sets the alter table action to add the given constraint.
   */
  public AlterTableQuery setAddConstraint(Constraint constraint) {
    return setAddCustomConstraint(constraint);
  }

  /**
   * Sets the alter table action to add the given constraint.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#toCustomConstraintClause}.
   */
  public AlterTableQuery setAddCustomConstraint(Object constraint) {
    return setAction(new AddConstraintAction(constraint));
  }

  /**
   * Sets the alter table action to add the given column.
   */
  public AlterTableQuery setAddColumn(Column column) {
    return setAddCustomColumn(column);
  }

  /**
   * Sets the alter table action to add the given column.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#toCustomTypedColumnSqlObject}.
   */
  public AlterTableQuery setAddCustomColumn(Object column) {
    return setAction(new AddColumnAction(column));
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
    _action.collectSchemaObjects(vContext);
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("ALTER TABLE ").append(_table).append(_action);
  }
  
  /**
   * "Action" for adding a unique constraint to a table.,
   * e.g. {@code "... ADD <constraint_clause>}.
   */
  public static class AddConstraintAction extends SqlObject
  {
    protected SqlObject _constraint;

    public AddConstraintAction(Object constraint) {
      _constraint = Converter.toCustomConstraintClause(constraint);
    }

    protected ConstraintClause getConstraint() {
      return (ConstraintClause)_constraint;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _constraint.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(" ADD ").append(_constraint);
    }
  }


  /**
   * "Action" for adding a column to a table.,
   * e.g. {@code "... ADD <column name> <column type> [constraints]}.
   *
   * @author Eric Fennell
   */
  public static class AddColumnAction extends SqlObject
  {
    private SqlObject _column;

    public AddColumnAction(Object column) {
      _column = Converter.toCustomTypedColumnSqlObject(column);
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _column.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(" ADD ").append(_column);
    }

    /**
     * Adds a constraint to the column specified in this action
     * <p>
     * {@code Object} -&gt; {@code SqlObject} constraint conversions handled by
     * {@link Converter#toCustomConstraintClause}.
     */
    public AddColumnAction addConstraint(Object constraint) {
      if(_column instanceof TypedColumnObject) {
        ((TypedColumnObject)_column).addConstraint(constraint);
      }
      return this;
    }

    /**
     * Sets the given value as the column default value for this action.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} value conversions handled by
     * {@link Converter#toValueSqlObject}.
     */
    public AddColumnAction setDefaultValue(Object defaultValue) {
      if(_column instanceof TypedColumnObject) {
        ((TypedColumnObject)_column).setDefaultValue(defaultValue);
      }
      return this;
    }
  }


  /**
   * "Action" for adding a unique constraint to a table.,
   * e.g. {@code "... ADD UNIQUE (<col1> ... [<coln>])}.
   * @deprecated use AddConstraintAction instead
   */
  @Deprecated
  public static class AddUniqueConstraintAction extends AddConstraintAction
  {
    public AddUniqueConstraintAction() {
      super(ConstraintClause.unique());
    }

    /**
     * Adds a column to the unique constraint definition.
     */
    public AddUniqueConstraintAction addColumns(Column... columns) {
      return addCustomColumns((Object[])columns);
    }

    /**
     * Adds a custom column to the unique constraint definition.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddUniqueConstraintAction addCustomColumns(Object... columnStrs) {
      getConstraint().addCustomColumns(columnStrs);
      return this;
    }
  }
  
  /**
   * "Action" for adding a primary key constraint to a table,
   * e.g. {@code "... ADD PRIMARY KEY (<col1> ... [<coln>])}.
   * @deprecated use AddConstraintAction instead
   */
  @Deprecated
  public static class AddPrimaryConstraintAction extends AddConstraintAction
  {
    public AddPrimaryConstraintAction() {
      super(ConstraintClause.primaryKey());
    }

    /**
     * Adds a column to the primary key constraint definition.
     */
    public AddPrimaryConstraintAction addColumns(Column... columns) {
      return addCustomColumns((Object[])columns);
    }

    /**
     * Adds a custom column to the primary key constraint definition.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddPrimaryConstraintAction addCustomColumns(Object... columnStrs) {
      getConstraint().addCustomColumns(columnStrs);
      return this;
    }
  }


  /**
   * "Action" for adding a foreign key constraint to a table,
   * e.g. 
   * {@code "... ADD FOREIGN KEY (<c1>...[<cn>]) REFERENCES t2 [(<c1>...<cn>)]}.
   * @deprecated use AddConstraintAction instead
   */
  @Deprecated
  public static class AddForeignConstraintAction extends AddConstraintAction
  {
    /** 
     * Creates a new {@link AddForeignConstraintAction} which references
     * the given {@link Table}.
     */
    public AddForeignConstraintAction(Table table) {
      this((Object)table);
    }

    /** 
     * Creates a new {@link AddForeignConstraintAction} which references
     * the given {@link Table}.
     *
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#toCustomTableSqlObject(Object)}.
     */
    public AddForeignConstraintAction(Object table) {
      super(ConstraintClause.foreignKey(table));
    }

    @Override
    protected ForeignKeyConstraintClause getConstraint() {
      return (ForeignKeyConstraintClause)super.getConstraint();
    }

    /** 
     * Adds {@code col} as a reference to the primary key in the referenced
     * table.
     */
    public AddForeignConstraintAction addPrimaryKeyReference(Column col) {
      return addReference(col, null);
    }

    /** 
     * Adds {@code col} as a reference to the primary key in the referenced
     * table.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddForeignConstraintAction addCustomPrimaryKeyReference(Object col){
      return addCustomReference(col, null);
    }

    /**
     * Adds {@code col} as a reference to {@code referencedCol} in the
     * referenced table.
     */
    public AddForeignConstraintAction addReference(
        Column col, Column referencedCol) {
      return addCustomReference(col, referencedCol);
    }

    /**
     * Adds {@code col} as a reference to {@code referencedCol} in the
     * referenced table.  If referencedCol is {@code null}, it is ignored
     * (useful for referencing the primary key in the referenced table).
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddForeignConstraintAction addCustomReference(
        Object col, Object referencedCol) {
      getConstraint().addCustomColumns(col);
      if (referencedCol != null) {
        getConstraint().addCustomRefColumns(referencedCol);
      }
      return this;
    }
  }
  
}

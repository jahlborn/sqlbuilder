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
import java.util.ListIterator;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.custom.HookAnchor;
import com.healthmarketscience.sqlbuilder.custom.oracle.OraTableSpaceClause;

/**
 * Query which generates a CREATE TABLE statement.
 * <p/>
 * Note that this query supports custom SQL syntax, see {@link Hook} for more
 * details.
 *
 * @author James Ahlborn
 */
public class CreateTableQuery extends BaseCreateQuery<CreateTableQuery>
{
  /**
   * The HookAnchors supported for CREATE TABLE queries.  See {@link com.healthmarketscience.sqlbuilder.custom}
   * for more details on custom SQL syntax.
   */
  public enum Hook implements HookAnchor {
    /** Anchor for the beginning of the query, only supports {@link
        HookType#BEFORE} */
    HEADER, 
    /** Anchor for the end of the query, only supports {@link
        HookType#BEFORE} */
    TRAILER;
  }
  
  /** column level constraints
   * @deprecated use {@link ConstraintClause} instead
   */
  @Deprecated
  public enum ColumnConstraint
  {
    NOT_NULL("NOT NULL"),
    UNIQUE("UNIQUE"),
    PRIMARY_KEY("PRIMARY KEY");

    private final String _constraintClause;

    private ColumnConstraint(String constraintClause) {
      _constraintClause = constraintClause;
    }
    
    @Override
    public String toString() { return _constraintClause; }
  }

  protected SqlObjectList<SqlObject> _constraints = SqlObjectList.create();
  
  public CreateTableQuery(Table table) {
    this(table, false);
  }

  /**
   * {@code Column} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#TYPED_COLUMN_TO_OBJ}.
   * 
   * @param table the table to create
   * @param includeColumns iff <code>true</code>, all the columns and
   *                       constraints of this table will be added to the
   *                       query
   */
  public CreateTableQuery(Table table, boolean includeColumns) {
    this((Object)table);

    if(includeColumns) {
      // add all the columns for this table
      _columns.addObjects(Converter.TYPED_COLUMN_TO_OBJ, table.getColumns());
      // add all the constraints for this table
      _constraints.addObjects(Converter.CUSTOM_TO_CONSTRAINTCLAUSE,
                              table.getConstraints());
    }
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateTableQuery(Object tableStr) {
    super(Converter.toCustomTableSqlObject(tableStr));
  }

  /**
   * @return a DropQuery for the object which would be created by this create
   *         query.
   */
  @Override
  public DropQuery getDropQuery() {
    return new DropQuery(DropQuery.Type.TABLE, _object);
  }
  
  /**
   * Adds the given Objects as column descriptions, should look like
   * <code>"&lt;column&gt; &lt;type&gt; [&lt;constraint&gt; ... ]"</code>.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#TYPED_COLUMN_TO_OBJ}.
   */
  @Override
  public CreateTableQuery addCustomColumns(Object... typedColumnStrs) {
    _columns.addObjects(Converter.TYPED_COLUMN_TO_OBJ, typedColumnStrs);
    return this;
  }

  /**
   * Adds column description for the given Column along with the given column
   * constraint.
   * @deprecated use {@link ConstraintClause} instead of ColumnConstraint
   */
  @Deprecated
  public CreateTableQuery addColumn(Column column, ColumnConstraint constraint)
  {
    return addCustomColumn(column, constraint);
  }

  /**
   * Adds given Object as column description along with the given column
   * constraint.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#TYPED_COLUMN_TO_OBJ}.
   * @deprecated use {@link ConstraintClause} instead of ColumnConstraint
   */
  @Deprecated
  public CreateTableQuery addCustomColumn(Object columnStr,
                                          ColumnConstraint constraint)
  {
    SqlObject column = Converter.TYPED_COLUMN_TO_OBJ.convert(columnStr);
    if(column instanceof TypedColumnObject) {
      ((TypedColumnObject)column).addConstraint(constraint);
    } else {
      column = new ConstrainedColumn(column, constraint);
    }
    _columns.addObject(column);
    return this;
  }

  /** Sets the constraint on a previously added column 
   * @deprecated use {@link ConstraintClause} instead of ColumnConstraint
   */
  @Deprecated
  public CreateTableQuery setColumnConstraint(Column column,
                                              ColumnConstraint constraint)
  {
    return addColumnConstraint(column, (Object)constraint);
  }
  
  /**
   * Adds the constraint on a previously added column
   * <p>
   * {@code Object} -&gt; {@code SqlObject} constraint conversions handled by
   * {@link Converter#toCustomConstraintClause}.
   */
  public CreateTableQuery addColumnConstraint(Column column, Object constraint)
  {
    for(ListIterator<SqlObject> iter = _columns.listIterator();
        iter.hasNext(); ) {
      SqlObject tmpCol = iter.next();
      if((tmpCol instanceof TypedColumnObject) &&
         (((TypedColumnObject)tmpCol)._column == column)) {
        // add constraint
        ((TypedColumnObject)tmpCol).addConstraint(constraint);
        break;
      }
    }
    return this;
  }
  
  /**
   * Sets the given value as the column default value on a previously added
   * column
   * <p>
   * {@code Object} -&gt; {@code SqlObject} value conversions handled by
   * {@link Converter#toValueSqlObject}.
   */
  public CreateTableQuery setColumnDefaultValue(Column column, Object defaultValue)
  {
    for(ListIterator<SqlObject> iter = _columns.listIterator();
        iter.hasNext(); ) {
      SqlObject tmpCol = iter.next();
      if((tmpCol instanceof TypedColumnObject) &&
         (((TypedColumnObject)tmpCol)._column == column)) {
        // add constraint
        ((TypedColumnObject)tmpCol).setDefaultValue(defaultValue);
        break;
      }
    }
    return this;
  }

  /**
   * Adds the given Constraints as table constraints.
   */
  public CreateTableQuery addConstraints(Constraint... constraints) {
    return addCustomConstraints((Object[])constraints);
  }

  /**
   * Adds the given Objects as table constraints, should look like
   * <code>"&lt;constraint&gt;"</code>.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_CONSTRAINTCLAUSE}.
   */
  public CreateTableQuery addCustomConstraints(Object... constraintStrs) {
    _constraints.addObjects(Converter.CUSTOM_TO_CONSTRAINTCLAUSE, 
                            constraintStrs);
    return this;
  }

  /** Sets a specific tablespace for the table to be created in by appending
   * <code>TABLESPACE &lt;tableSpace&gt;</code> to the end of the CREATE
   * query.
   *  <p>
   *  <em>WARNING, this is not ANSI SQL compliant.</em>
   *
   * @see OraTableSpaceClause
   * 
   * @deprecated Use {@code addCustomization(new OraTableSpaceClause(tableSpace))}
   *             instead.
   */
  @Deprecated
  public CreateTableQuery setTableSpace(String tableSpace) {
    return addCustomization(new OraTableSpaceClause(tableSpace));
  }
  
  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param hook the part of the query being customized
   * @param type the type of customization
   * @param obj the custom sql.  The {@code Object} -&gt; {@code SqlObject}
   *            conversions handled by {@link Converter#toCustomSqlObject}.
   */
  public CreateTableQuery addCustomization(Hook hook, HookType type, Object obj) {
    super.addCustomization(hook, type, obj);
    return this;
  }
  
  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param obj the custom sql syntax on which the 
   *            {@link CustomSyntax#apply(CreateTableQuery)} method will be
   *            invoked (may be {@code null}).
   */
  public CreateTableQuery addCustomization(CustomSyntax obj) {
    if(obj != null) {
      obj.apply(this);
    }
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _constraints.collectSchemaObjects(vContext);
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // validate super
    super.validate(vContext);

    // we'd better have some columns
    if(_columns.isEmpty()) {
      throw new ValidationException("Table has no columns");
    }
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    customAppendTo(app, Hook.HEADER);

    app.append("CREATE TABLE ").append(_object)
      .append(" (").append(_columns);
    if(!_constraints.isEmpty()) {
      app.append(",").append(_constraints);
    }
    app.append(")");

    customAppendTo(app, Hook.TRAILER);
  }
  
  /**
   * Wrapper around a column that adds a constraint specification.
   */
  private static class ConstrainedColumn extends SqlObject
  {
    private SqlObject _column;
    private Object _constraint;
  
    private ConstrainedColumn(SqlObject column, Object constraint) {
      _column = column;
      _constraint = constraint;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _column.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(_column).append(" ").append(_constraint);
    }
  }

}

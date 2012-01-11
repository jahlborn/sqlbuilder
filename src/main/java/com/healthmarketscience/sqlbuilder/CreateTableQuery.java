/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.util.ListIterator;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Query which generates a CREATE TABLE statement.
 *
 * @author James Ahlborn
 */
public class CreateTableQuery extends BaseCreateQuery<CreateTableQuery>
{

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
    
    app.append("CREATE TABLE ").append(_object)
      .append(" (").append(_columns);
    if(!_constraints.isEmpty()) {
      app.append(",").append(_constraints);
    }
    app.append(")");
    appendTableSpace(app);
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

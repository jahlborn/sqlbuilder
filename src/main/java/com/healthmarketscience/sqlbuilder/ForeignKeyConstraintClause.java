/*
Copyright (c) 2011 James Ahlborn

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
  protected SqlObject _refTable;
  protected SqlObjectList<SqlObject> _refColumns = SqlObjectList.create();

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
    super.appendTo(app);

    if(SqlContext.getContext(app).getUseTableConstraints()) {
      app.append(" ").append(_type.toString(false));
    }
    app.append(" ").append(_refTable);
    if(!_refColumns.isEmpty()) {
      app.append(" (").append(_refColumns).append(")");
    }
  }
}

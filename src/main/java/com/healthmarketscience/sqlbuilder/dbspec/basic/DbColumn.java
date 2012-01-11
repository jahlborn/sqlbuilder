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

package com.healthmarketscience.sqlbuilder.dbspec.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import java.sql.Types;

/**
 * Representation of a column in a database schema.
 *
 * @author James Ahlborn
 */
public class DbColumn extends DbObject<DbTable>
  implements Column
{
  private static final Map<Integer,String> _typeNameMap = new HashMap<Integer,String>();
  static {
    try {
      // create a type -> type name map using the name of the constant field
      for(java.lang.reflect.Field typeField : Types.class.getFields()) {
        int mods = typeField.getModifiers();
        if(java.lang.reflect.Modifier.isPublic(mods) &&
           java.lang.reflect.Modifier.isStatic(mods) &&
           (typeField.getType() == int.class)) {
          Integer val = (Integer)typeField.get(null);
          _typeNameMap.put(val, typeField.getName());
        }
      }
    } catch(Exception e) {
      // should never happen
      throw new Error("<clinit> cannot access jdbc type constants", e);
    }
  }
    
  private final String _typeName;
  private final Integer _typeLength;
  private final List<DbConstraint> _constraints = new ArrayList<DbConstraint>();
  private Object _defaultValue;

  public DbColumn(DbTable parent, String name,
                  String typeName, Integer typeLength) {
    super(parent, name);
    _typeName = typeName;
    _typeLength = typeLength;
  }

  public DbTable getTable() {
    return getParent();
  }
    
  public String getColumnNameSQL() {
    return getName();
  }
    
  public String getTypeNameSQL() {
    return _typeName;
  }
    
  public Integer getTypeLength() {
    return _typeLength;
  }

  public List<DbConstraint> getConstraints() {
    return _constraints;
  }

  /**
   * Sets the default value for this column.  A value of {@code null} will
   * be treated as <i>no</i> default value.  
   */
  public DbColumn setDefaultValue(Object defaultValue) {
    _defaultValue = defaultValue;
    return this;
  }

  public Object getDefaultValue() {
    return _defaultValue;
  }

  /**
   * Creates and adds not null constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @return the freshly created constraint
   */
  public DbConstraint notNull() {
    return notNull(null);
  }

  /**
   * Creates and adds not null constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @return the freshly created constraint
   */
  public DbConstraint notNull(String name) {
    DbConstraint constraint = getSpec().createColumnConstraint(
        this, name, Constraint.Type.NOT_NULL);
    return addConstraint(constraint);
  }

  /**
   * Creates and adds unique constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @return the freshly created constraint
   */
  public DbConstraint unique() {
    return unique(null);
  }

  /**
   * Creates and adds unique constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @return the freshly created constraint
   */
  public DbConstraint unique(String name) {
    DbConstraint constraint = getSpec().createColumnConstraint(
        this, name, Constraint.Type.UNIQUE);
    return addConstraint(constraint);
  }

  /**
   * Creates and adds primary key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @return the freshly created constraint
   */
  public DbConstraint primaryKey() {
    return primaryKey(null);
  }

  /**
   * Creates and adds primary key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @return the freshly created constraint
   */
  public DbConstraint primaryKey(String name) {
    DbConstraint constraint = getSpec().createColumnConstraint(
        this, name, Constraint.Type.PRIMARY_KEY);
    return addConstraint(constraint);
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param referencedTableName the name of the referenced table
   * @return the freshly created constraint
   */
  public DbForeignKeyConstraint references(String referencedTableName) {
    return references(null, referencedTableName);
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param referencedTableName the name of the referenced table
   * @return the freshly created constraint
   */
  public DbForeignKeyConstraint references(String name, 
                                           String referencedTableName) {
    return references(name, referencedTableName, null);
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param referencedTableName the name of the referenced table
   * @param referencedColName the names of the referenced column
   * @return the freshly created constraint
   */
  public DbForeignKeyConstraint references(String name, 
                                           String referencedTableName, 
                                           String referencedColName) {
    DbTable table = getTable().getParent().findTable(referencedTableName);
    DbForeignKeyConstraint fkConstraint =
      getSpec().createColumnForeignKeyConstraint(
        this, name, table, referencedColName);
    return addConstraint(fkConstraint);
  }

  /**
   * Adds the given constraint to this column.
   * <p>
   * Note, no effort is made to make sure the given constraint is unique.
   * @param constraint the constraint to be added
   * @return the given constraint
   */
  public <T extends DbConstraint> T addConstraint(T constraint) {
    _constraints.add(checkOwnership(constraint));
    return constraint;
  }
  
  /**
   * Returns the standard jdbc type name for the give type value (one of {@link java.sql.Types}).
   */
  public static String getTypeName(int type)
  {
    String name = _typeNameMap.get(type);
    if(name == null) {
      throw new IllegalArgumentException("Type " + type + " is not a valid sql type");
    }
    return name;
  }
}

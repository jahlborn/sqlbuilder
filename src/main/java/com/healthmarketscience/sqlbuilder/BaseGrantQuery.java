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

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Base of a query which generates a query for manipulating privileges.  Keeps
 * track of the privileges, grantees, and target object.
 *
 * @author James Ahlborn
 */
public abstract class BaseGrantQuery<ThisType extends BaseGrantQuery<ThisType>>
  extends Query<ThisType>
{

  /** grantee object which represents PUBLIC access */
  public static final SqlObject PUBLIC_GRANTEE = new CustomSql("PUBLIC");

  private static final Privilege PRIVILEGE_ALL =
    new Privilege(Privilege.Type.ALL);
  private static final Privilege PRIVILEGE_SELECT =
    new Privilege(Privilege.Type.SELECT);
  private static final Privilege PRIVILEGE_DELETE =
    new Privilege(Privilege.Type.DELETE);
  private static final Privilege PRIVILEGE_USAGE =
    new Privilege(Privilege.Type.USAGE);
  
  protected SqlObjectList<SqlObject> _grantees = SqlObjectList.create();
  protected SqlObjectList<SqlObject> _privileges = SqlObjectList.create();
  protected SqlObject _targetObj;

  
  protected BaseGrantQuery() {
  }

  /**
   * Adds the given custom grantees to the query
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_OBJ}.
   */
  public ThisType addCustomGrantees(Object... grantees) {
    _grantees.addObjects(Converter.CUSTOM_TO_OBJ, grantees);
    return getThisType();
  }    
    
  /** Adds the given grantees to the query */
  public ThisType addGrantees(String... grantees) {
    return addCustomGrantees((Object[])grantees);
  }

  /**
   * Adds the given privileges to the query.  Generally this should be an
   * instance of a {@link Privilege} object, created by one of the static
   * <code>privilege*</code> methods.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_OBJ}.
   */
  public ThisType addCustomPrivileges(Object... privileges) {
    _privileges.addObjects(Converter.CUSTOM_TO_OBJ, privileges);
    return getThisType();
  }

  /** Adds the given privileges to the query.  Generally this should be an
      instance of a {@link Privilege} object, created by one of the static
      <code>privilege*</code> methods. */
  public ThisType addPrivileges(Privilege... privileges) {
    return addCustomPrivileges((Object[])privileges);
  }

  /**
   * Sets the target for the query.  Generally this should be an
   * instance of a {@link TargetObject} object, created by one of the static
   * <code>target*</code> methods.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public ThisType setCustomTarget(Object target) {
    _targetObj = Converter.toCustomSqlObject(target);
    return getThisType();
  }
  
  /** Sets the target for the query.  Generally this should be an
      instance of a {@link TargetObject} object, created by one of the static
      <code>target*</code> methods. */
  public ThisType setTarget(TargetObject target) {
    return setCustomTarget(target);
  }

  /** @return a Privilege with the type of ALL */
  public static Privilege privilegeAll() {
    return PRIVILEGE_ALL;
  }

  /** @return a Privilege with the type of SELECT */
  public static Privilege privilegeSelect() {
    return PRIVILEGE_SELECT;
  }

  /** @return a Privilege with the type of DELETE */
  public static Privilege privilegeDelete() {
    return PRIVILEGE_DELETE;
  }

  /** @return a Privilege with the type of INSERT and the given (optional)
      columns */
  public static Privilege privilegeInsert(Column... columns) {
    return new Privilege(Privilege.Type.INSERT, columns);
  }

  /** @return a Privilege with the type of UPDATE and the given (optional)
      columns */
  public static Privilege privilegeUpdate(Column... columns) {
    return new Privilege(Privilege.Type.UPDATE, columns);
  }
  
  /** @return a Privilege with the type of REFERENCES and the given (optional)
      columns */
  public static Privilege privilegeReferences(Column... columns) {
    return new Privilege(Privilege.Type.REFERENCES, columns);
  }

  /** @return a Privilege with the type of USAGE */
  public static Privilege privilegeUsage() {
    return PRIVILEGE_USAGE;
  }

  /** @return a TargetObject with the type of TABLE and the given table */
  public static TargetObject targetTable(Table table) {
    return new TargetObject(TargetObject.Type.TABLE, new TableObject(table));
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _grantees.collectSchemaObjects(vContext);
    _privileges.collectSchemaObjects(vContext);
    _targetObj.collectSchemaObjects(vContext);
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // validate super class
    super.validate(vContext);

    // must have privileges
    if(_privileges.isEmpty()) {
      throw new ValidationException("Must specify privileges");
    }
    // must have grantees
    if(_grantees.isEmpty()) {
      throw new ValidationException("Must specify grantees");
    }
    // if no _targetObj, NPE will be thrown already

    // cannot have additional privileges with ALL
    boolean hasAll = false;
    for(SqlObject privilege : _privileges) {
      if((privilege instanceof Privilege) &&
         (((Privilege)privilege)._type == Privilege.Type.ALL)) {
        hasAll = true;
        break;
      }
    }
    if(hasAll && (_privileges.size() > 1)) {
      throw new ValidationException("May not have other privileges with ALL");
    }
  }

  /**
   * Encapsulation of a database privilege.
   */
  public static class Privilege extends SqlObject
  {
    /** Enumeration representing the various database privilege types */
    public enum Type {
      ALL("ALL PRIVILEGES", false),
      SELECT("SELECT", false),
      DELETE("DELETE", false),
      INSERT("INSERT", true),
      UPDATE("UPDATE", true),
      REFERENCES("REFERENCES", true),
      USAGE("USAGE", false);

      private final String _typeStr;
      private final boolean _maySpecifyColumns;

      private Type(String typeStr, boolean maySpecifyColumns) {
        _typeStr = typeStr;
        _maySpecifyColumns = maySpecifyColumns;
      }

      public boolean maySpecifyColumns() {
        return _maySpecifyColumns;
      }
      
      @Override
      public String toString() { return _typeStr; }      
    }

    private Type _type;
    private SqlObjectList<SqlObject> _columns = SqlObjectList.create();

    public Privilege(Type type, Column... columns) {
      _type = type;
      if(_type.maySpecifyColumns()) {
        _columns.addObjects(Converter.COLUMN_TO_OBJ, columns);
      }
    }
    
    /**
     * Adds the given columns to the column list.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public Privilege addCustomColumns(Object... columnStrs) {
      if(_type.maySpecifyColumns()) {
        _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
      }
      return this;
    }

    /** Adds the given columns to the column list. */
    public Privilege addColumns(Column... columns) {
      return addCustomColumns((Object[])columns);
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _columns.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(_type);
      if(!_columns.isEmpty()) {
        app.append("(").append(_columns).append(")");
      }
    }    
  }

  /**
   * Information about the database object upon which a privilege allows (or
   * disallows) action.
   */
  public static class TargetObject extends SqlObject
  {
    /** Enumeration representing the types of database objects which have
        privileges for interacting with them. */
    public enum Type {
      TABLE("TABLE "),
      DOMAIN("DOMAIN "),
      COLLATION("COLLATION "),
      CHARACTER_SET("CHARACTER SET "),
      TRANSLATION("TRANSLATION ");

      private final String _typeStr;

      private Type(String typeStr) {
        _typeStr = typeStr;
      }

      @Override
      public String toString() { return _typeStr; }      
    }

    private Type _type;
    private SqlObject _name;

    public TargetObject(Type type, SqlObject name) {
      _type = type;
      _name = name;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _name.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(_type).append(_name);
    }    
  }
  
}

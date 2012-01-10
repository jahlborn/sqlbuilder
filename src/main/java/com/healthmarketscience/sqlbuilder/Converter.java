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

import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Function;
import com.healthmarketscience.sqlbuilder.dbspec.Index;
import com.healthmarketscience.sqlbuilder.dbspec.Table;


/**
 * Class which encapsulates various object to SqlObject conversions.
 *
 * @author James Ahlborn
 */
public abstract class Converter<SrcType, DstType>
{
  /**
   * Converter which converts an Object to a CustomSql using
      {@link #toCustomSqlObject(Object)} */
  public static final Converter<Object, SqlObject> CUSTOM_TO_OBJ =
    new Converter<Object, SqlObject>() {
    @Override
      public SqlObject convert(Object obj) {
        return toCustomSqlObject(obj);
      }
    };

  /**
   * Converter which converts a Column to a ColumnObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * </ul>
   */
  public static final Converter<Column, SqlObject> COLUMN_TO_OBJ =
    new Converter<Column, SqlObject>() {
    @Override
    public SqlObject convert(Column col) {
        return ((col != null) ? new ColumnObject(col) : SqlObject.NULL_VALUE);
      }
    };

  /** Converter which converts a value object to a SqlObject using
      {@link #toValueSqlObject(Object)} */
  public static final Converter<Object, SqlObject> VALUE_TO_OBJ =
    new Converter<Object, SqlObject>() {
      @Override
      public SqlObject convert(Object value) {
        return toValueSqlObject(value);
      }
    };

  /** Converter which converts a column value object to a SqlObject using
      {@link #toColumnSqlObject(Object)} */
  public static final Converter<Object, SqlObject> COLUMN_VALUE_TO_OBJ =
    new Converter<Object, SqlObject>() {
      @Override
      public SqlObject convert(Object value) {
        return toColumnSqlObject(value);
      }
    };

  /** Converter which converts a custom column object to a SqlObject using
      {@link #toCustomColumnSqlObject(Object)} */
  public static final Converter<Object, SqlObject> CUSTOM_COLUMN_TO_OBJ =
    new Converter<Object, SqlObject>() {
      @Override
      public SqlObject convert(Object value) {
        return toCustomColumnSqlObject(value);
      }
    };

  /**
   * Converter which converts an Object to an Expression using
   * {@link #toExpressionObject(Object)}
   */
  public static final Converter<Object, Expression> CUSTOM_TO_EXPRESSION =
    new Converter<Object, Expression>() {
    @Override
      public Expression convert(Object obj) {
        return toExpressionObject(obj);
      }
    };
    
  /**
   * Converter which converts an Object to a Condition using
   * {@link #toConditionObject(Object)}
   */
  public static final Converter<Object, Condition> CUSTOM_TO_CONDITION =
    new Converter<Object, Condition>() {
    @Override
      public Condition convert(Object obj) {
        return toConditionObject(obj);
      }
    };
  
  
  /** Converter which converts a Column to a TypedColumnObject or a value
      object to a SqlObject using {@link #toCustomSqlObject(Object)} */
  public static final Converter<Object, SqlObject> TYPED_COLUMN_TO_OBJ =
    new Converter<Object, SqlObject>() {
    @Override
      public SqlObject convert(Object value) {
        return toCustomTypedColumnSqlObject(value);
      }
    };


  /** Converter which converts an Object to a Subquery using
      {@link #toSubquery} */
  public static final Converter<Object, Subquery> CUSTOM_TO_SUBQUERY =
    new Converter<Object, Subquery>() {
    @Override
      public Subquery convert(Object value) {
        return toSubquery(value);
      }
    };

  /** Converter which converts an Object to a SqlObject using
      {@link #toCustomConstraintSqlObject} */
  public static final Converter<Object, SqlObject> CUSTOM_TO_CONSTRAINTCLAUSE =
    new Converter<Object, SqlObject>() {
    @Override
      public SqlObject convert(Object value) {
        return toCustomConstraintClause(value);
      }
    };

  /** Converter which converts a custom table def object to a SqlObject using
      {@link #toCustomTableDefSqlObject(Object)} */
  public static final Converter<Object, SqlObject> CUSTOM_TABLE_DEF_TO_OBJ =
    new Converter<Object, SqlObject>() {
      @Override
      public SqlObject convert(Object value) {
        return toCustomTableDefSqlObject(value);
      }
    };
  

  /**
   * Converts the given src object to a SqlObject of the expected type.
   * 
   * @param src object to be coerced
   * @return the src object coerced to the desired type
   */
  public abstract DstType convert(SrcType src);

  
  /**
   * Converts a Column to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * </ul>
   * 
   * @param col object to coerce to a column SqlObject
   * @return a SqlObject for the given Column appropriate for referencing
   *         the column most anywhere in a query
   */
  public static SqlObject toColumnSqlObject(Column col) {
    return new ColumnObject(col);
  }
  
  /**
   * Converts a Column to a SqlObject with the given alias.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * </ul>
   * <p>
   * Result of previous conversion is wrapped as an {@link AliasedObject} if
   * the given alias is non-{@code null}.
   * 
   * @param col object to coerce to a column SqlObject
   * @param alias optional column alias for the object
   * @return a SqlObject for the given Column with the given alias
   *         appropriate for referencing the column most anywhere in a query
   */
  public static SqlObject toColumnSqlObject(Column col, String alias) {
    return AliasedObject.toAliasedObject(toColumnSqlObject(col), alias);
  }
  
  /**
   * Converts a Table to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Table} -&gt; {@link TableObject}</li>
   * </ul>
   * 
   * @param table object to coerce to a table SqlObject
   * @return a SqlObject for the given Table
   */
  public static SqlObject toTableSqlObject(Table table) {
    return new TableObject(table);
  }
  
  /**
   * Converts a Constraint to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Constraint} -&gt; {@link ConstraintObject}</li>
   * </ul>
   * 
   * @param constraint object to coerce to a constraint SqlObject
   * @return a SqlObject for the given Constraint
   */
  public static SqlObject toConstraintSqlObject(Constraint constraint) {
    return new ConstraintObject(constraint);
  }
  
  /**
   * Converts a Table to a table definition SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Table} -&gt; {@link TableDefObject}</li>
   * </ul>
   * 
   * @param table object to coerce to a table SqlObject
   * @return a SqlObject for the given Table
   */
  public static SqlObject toTableDefSqlObject(Table table) {
    return new TableDefObject(table);
  }
  
  /**
   * Converts a Index to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Index} -&gt; {@link IndexObject}</li>
   * </ul>
   * 
   * @param index object to coerce to a index SqlObject
   * @return a SqlObject for the given Index
   */
  public static SqlObject toIndexSqlObject(Index index) {
    return new IndexObject(index);
  }
  
  /**
   * Converts a Function to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Function} -&gt; {@link FunctionObject}</li>
   * </ul>
   * 
   * @param function object to coerce to a function SqlObject
   * @return a SqlObject for the given Function
   */
  public static SqlObject toFunctionSqlObject(Function function) {
    return new FunctionObject(function);
  }
    
  /**
   * Converts a column Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link ValueObject}</li>
   * </ul>
   * 
   * @param obj object to coerce to a column SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toColumnSqlObject(Object obj) {
    if(obj instanceof Column) {
      return toColumnSqlObject((Column)obj);
    }
    return toValueSqlObject(obj);
  }
  
  /**
   * Converts a column Object to a SqlObject with the given alias.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link ValueObject}</li>
   * </ul>
   * <p>
   * Result of previous conversion is wrapped as an {@link AliasedObject} if
   * the given alias is non-{@code null}.
   * 
   * @param obj object to coerce to a column SqlObject
   * @param alias optional column alias for the object
   * @return a SqlObject for the given Object with the given alias.
   */
  public static SqlObject toColumnSqlObject(Object obj, String alias) {
    return AliasedObject.toAliasedObject(toColumnSqlObject(obj), alias);
  }

  /**
   * Converts a column Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom column SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomColumnSqlObject(Object obj) {
    if(obj instanceof Column) {
      return toColumnSqlObject((Column)obj);
    }
    return toCustomSqlObject(obj);
  }
  
  /**
   * Converts a column Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom column SqlObject
   * @param alias optional column alias for the object
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomColumnSqlObject(Object obj, String alias) {
    return AliasedObject.toAliasedObject(toCustomColumnSqlObject(obj), alias);
  }
  
  /**
   * Converts a table Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Table} -&gt; {@link TableObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom table SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomTableSqlObject(Object obj) {
    if(obj instanceof Table) {
      return toTableSqlObject((Table)obj);
    }
    return toCustomSqlObject(obj);
  }
  
  /**
   * Converts a constraint Object to a SqlObject for use in a constraint
   * clause..
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Constraint} -&gt; {@link ConstraintObject}</li>
   * <li>{@code null} -&gt; {@code null}</li>
   * </ul>
   * If none of the previous conversions are applied, the following
   * conversions are tried in order, then wrapped in a
   * {@link ConstraintClause.Prefix}.
   * <ul>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom constraint SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomConstraintSqlObject(Object obj) {
    if(obj instanceof Constraint) {
      return toConstraintSqlObject((Constraint)obj);
    }
    if(obj == null) {
      return null;
    }
    return new ConstraintClause.Prefix(toCustomSqlObject(obj));
  }
  
  /**
   * Converts a index Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Index} -&gt; {@link IndexObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom index SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomIndexSqlObject(Object obj) {
    if(obj instanceof Index) {
      return toIndexSqlObject((Index)obj);
    }
    return toCustomSqlObject(obj);
  }
  
  /**
   * Converts a function Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Function} -&gt; {@link FunctionObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom function SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomFunctionSqlObject(Object obj) {
    if(obj instanceof Function) {
      return toFunctionSqlObject((Function)obj);
    }
    return toCustomSqlObject(obj);
  }
    
  /**
   * Converts a value Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link ValueObject}</li>
   * </ul>
   * 
   * @param obj object to coerce to a value SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toValueSqlObject(Object obj) {
    if(obj == null) {
      return SqlObject.NULL_VALUE;
    } else if(obj instanceof Boolean) {
      return new BooleanValueObject((Boolean)obj);
    } else if(obj instanceof Number) {
      return new NumberValueObject((Number)obj);
    } else if(obj instanceof SqlObject) {
      return (SqlObject)obj;
    }
    return new ValueObject(obj);
  }
    
  /**
   * Converts a value Object to a SqlObject with the given alias.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link ValueObject}</li>
   * </ul>
   * <p>
   * Result of previous conversion is wrapped as an {@link AliasedObject} if
   * the given alias is non-{@code null}.
   * 
   * @param obj object to coerce to a value SqlObject
   * @param alias optional column alias for the object
   * @return a SqlObject for the given Object with the given alias.
   */
  public static SqlObject toValueSqlObject(Object obj, String alias) {
    return AliasedObject.toAliasedObject(toValueSqlObject(obj), alias);
  }

  /**
   * Converts an Object to a custom SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a custom SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomSqlObject(Object obj) {
    SqlObject rtnObj;
    if(obj == null) {
      rtnObj = SqlObject.NULL_VALUE;
    } else if(obj instanceof SqlObject) {
      rtnObj = (SqlObject)obj;
    } else if(obj instanceof Boolean) {
      return new BooleanValueObject((Boolean)obj);
    } else if(obj instanceof Number) {
      rtnObj = new NumberValueObject((Number)obj);
    } else {
      rtnObj = new CustomSql(obj);
    }
    return rtnObj;
  }

  /**
   * Converts an Object to a custom SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * <p>
   * Result of previous conversion is wrapped as an {@link AliasedObject} if
   * the given alias is non-{@code null}.
   * 
   * @param obj object to coerce to a custom SqlObject
   * @param alias optional column alias for the object
   * @return a SqlObject for the given Object with the given alias.
   */
  public static SqlObject toCustomSqlObject(Object obj, String alias) {
    return AliasedObject.toAliasedObject(toCustomSqlObject(obj), alias);
  }

  /**
   * Converts a table Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Table} -&gt; {@link TableDefObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param table object to coerce to a table definition SqlObject
   * @return the given table Object wrapped as a SqlObject.
   */
  public static SqlObject toCustomTableDefSqlObject(Object table)
  {
    if(table instanceof Table) {
      return toTableDefSqlObject((Table)table);
    }
    return toCustomSqlObject(table);
  }

  /**
   * Converts a column Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link TypedColumnObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param column object to coerce to a typed column definition SqlObject
   * @return the given table Object wrapped as a SqlObject.
   */
  public static SqlObject toCustomTypedColumnSqlObject(Object column)
  {
    if(column instanceof Column) {
      return new TypedColumnObject((Column)column);
    }
    return toCustomSqlObject(column);
  }

  /**
   * Converts an Object to an Expression.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code Expression} -&gt; {@code Expression}</li>
   * <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>Result of the conversions below wrapped as a {@link CustomExpression}</li>
   *   <ul>
   *   <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   *   <li>{@link java.lang.Object} -&gt; {@link ValueObject}</li>
   *   </ul>
   * </ul>
   * 
   * @param obj object to coerce to an Expression
   * @return the given Object as an Expression
   */
  public static Expression toExpressionObject(Object obj) {
    if(obj instanceof Expression) {
      return (Expression)obj;
    }
    obj = toColumnSqlObject(obj);
    // the wrapper may be an Expression, so check again
    return ((obj instanceof Expression) ?
            (Expression)obj : new CustomExpression(obj));
  }

  /**
   * Converts an Object to a Condition.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@code Condition} -&gt; {@code Condition}</li>
   * <li>Result of the conversions below wrapped as a {@link CustomCondition}</li>
   *   <ul>
   *   <li>{@link com.healthmarketscience.sqlbuilder.dbspec.Column} -&gt; {@link ColumnObject}</li>
   *   <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   *   <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   *   <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   *   <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   *   <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   *   </ul>
   * </ul>
   *
   * @param obj object to coerce to a Condition
   * @return the given Object as a Condition
   */
  public static Condition toConditionObject(Object obj) {
    if(obj instanceof Condition) {
      return (Condition)obj;
    }
    return new CustomCondition(toCustomColumnSqlObject(obj));
  }

  /**
   * Converts an Object to a Subquery.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link Subquery} -&gt; {@link Subquery}</li>
   * <li>{@link java.lang.Object} -&gt; {@link Subquery}</li>
   * </ul>
   * 
   * @param obj object to coerce to a Subquery
   * @return a Subquery for the given Object.
   */
  public static Subquery toSubquery(Object obj) {
    return ((obj instanceof Subquery) ? (Subquery)obj : new Subquery(obj));
  }

  /**
   * Converts an constraint clause Object to a SqlObject.
   * <p>
   * Conversions (in order):
   * <ul>
   * <li>{@link Constraint} -&gt; {@link ConstraintClause}</li>
   * <li>{@code null} -&gt; {@link SqlObject#NULL_VALUE}</li>
   * <li>{@link SqlObject} -&gt; {@link SqlObject}</li>
   * <li>{@link java.lang.Boolean} -&gt; {@link BooleanValueObject}</li>
   * <li>{@link java.lang.Number} -&gt; {@link NumberValueObject}</li>
   * <li>{@link java.lang.Object} -&gt; {@link CustomSql}</li>
   * </ul>
   * 
   * @param obj object to coerce to a constraint clause SqlObject
   * @return a SqlObject for the given Object.
   */
  public static SqlObject toCustomConstraintClause(Object obj) {
    if(obj instanceof Constraint) {
      return ConstraintClause.from((Constraint)obj);
    }
    return toCustomSqlObject(obj);
  }

}

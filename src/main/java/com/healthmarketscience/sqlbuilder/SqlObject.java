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
import com.healthmarketscience.common.util.AppendeeObject;
import com.healthmarketscience.common.util.StringAppendableExt;



/**
 * Base object which all classes in this facility extend.  It is an
 * AppendeeObject so that the statement building can happen efficiently
 * using the {@link #appendTo} calls.  Additionally, there is a
 * {@link #collectSchemaObjects} method which is used for validation (more
 * details in the method comment).
 *
 * @author James Ahlborn
 */
public abstract class SqlObject extends AppendeeObject
{
  /** SqlObject which represents a <code>?</code> string for generating
      prepared statements. */
  public static final SqlObject QUESTION_MARK = new Expression()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("?");
      }
      @Override
      protected void collectSchemaObjects(ValidationContext vContext) {}
    };

  /** SqlObject which represents a <code>*</code> string for generating
      "SELECT *" statements. */
  public static final SqlObject ALL_SYMBOL = new SqlObject()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("*");
      }
      @Override
      protected void collectSchemaObjects(ValidationContext vContext) {}
    };

  /** SqlObject which represents a <code>NULL</code> value string */
  public static final SqlObject NULL_VALUE = new Expression()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("NULL");
      }
      @Override
      protected void collectSchemaObjects(ValidationContext vContext) {}
    };

  protected SqlObject() {
  }

  /**
   * Creates a SQL string from this object using the given initial size for
   * the AppendableExt buffer and the given SqlContext.  Useful for
   * introducing custom contexts or context settings to the SQL generation.
   * @param size initial size of the output buffer
   * @param context optional custom SqlContext for the SQL generation
   * @return the generated SQL query
   */
  public String toString(int size, SqlContext context) {
    StringAppendableExt app = new StringAppendableExt(size);
    app.setContext(context);
    return app.append(this).toString();
  }  

  /**
   * Utility method for implementing the {@link Verifiable#validate()} method.
   */
  protected void doValidate() throws ValidationException
  {
    ValidationContext vContext = new ValidationContext();

    // collect the validation information
    collectSchemaObjects(vContext);

    // validate everything that's verifiable
    vContext.validateAll();
  }
    
  /**
   * Used during Query.validate() calls to collect the dbschema objects
   * referenced in a query.  Any subclass of this class should add all
   * referenced tables and columns to the appropriate collections.
   * @param vContext handle to the current validation context
   */
  protected abstract void collectSchemaObjects(ValidationContext vContext);


}

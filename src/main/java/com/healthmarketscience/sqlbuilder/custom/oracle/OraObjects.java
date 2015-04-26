/*
Copyright (c) 2015 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom.oracle;

import com.healthmarketscience.sqlbuilder.ValidationContext;
import java.io.IOException;
import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.Expression;

/**
 * Miscellaneous useful constructs for custom Oracle syntax.
 *
 * @author James Ahlborn
 */
public class OraObjects 
{
  /** SqlObject which represents the Oracle {@code ROWNUM} pseudo-column. */
  public static final SqlObject ROWNUM = new Expression()
    {
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        app.append("ROWNUM");
      }
      @Override
      protected void collectSchemaObjects(ValidationContext vContext) {}
    };


  private OraObjects() {}

}

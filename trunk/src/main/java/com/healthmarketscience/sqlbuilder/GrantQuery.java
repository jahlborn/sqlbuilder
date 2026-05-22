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

/**
 * Query which generates a GRANT (privileges) statement.
 *
 * @author James Ahlborn
 */
public class GrantQuery extends BaseGrantQuery<GrantQuery>
{

  private boolean _allowGranteeToGrant;
  
  public GrantQuery() {
  }

  /** Sets whether or not grantee is allowed to grant these privileges to
      others */
  public GrantQuery setAllowGranteeToGrant(boolean newAllowGranteeToGrant) {
    _allowGranteeToGrant = newAllowGranteeToGrant;
    return this;
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("GRANT ").append(_privileges).append(" ON ")
      .append(_targetObj).append(" TO ").append(_grantees);
    if(_allowGranteeToGrant) {
      app.append(" WITH GRANT OPTION");
    }
  }
  
}

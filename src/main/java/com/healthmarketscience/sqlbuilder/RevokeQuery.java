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

/**
 * Query which generates a REVOKE (privileges) statement.
 *
 * @author James Ahlborn
 */
public class RevokeQuery extends BaseGrantQuery
{

  private DropQuery.Behavior _behavior;
  
  public RevokeQuery() {
  }

  /** Sets the behavior for the revoke query */
  public RevokeQuery setBehavior(DropQuery.Behavior newBehavior) {
    _behavior = newBehavior;
    return this;
  }  

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("REVOKE ").append(_privileges).append(" ON ")
      .append(_targetObj).append(" FROM ").append(_grantees);
    if(_behavior != null) {
      app.append(_behavior);
    }
  }

}

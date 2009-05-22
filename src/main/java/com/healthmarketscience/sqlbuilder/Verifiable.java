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

/**
 * Interface for SqlObjects wishing to provide verifiablity.  In general, all
 * checking in SqlObject implementations is deferred until an object is fully
 * constructed.  As a post process, a user may choose to validate the sql
 * construct after it is fully constructed by calling the validate method.
 *
 * @author James Ahlborn
 */
public interface Verifiable<ThisType extends Verifiable<ThisType>> {

  /**
   * Runs validation on this verifiable object.
   * 
   * @return a handle to this instance
   */
  public ThisType validate() throws ValidationException;
  
  /**
   * Runs validation on this verifiable object using a previously collected
   * ValidationContext.
   * <p>
   * In general, this method will only be called internally, not by users.
   *
   * @param vContext handle to the current, filled-in validation context
   */
  public void validate(ValidationContext vContext)
    throws ValidationException;
  
}

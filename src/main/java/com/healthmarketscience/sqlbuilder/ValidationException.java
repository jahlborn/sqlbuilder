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

import com.healthmarketscience.common.util.Tuple2;


/**
 * Indicates that a sql builder query is not valid.  This is a runtime
 * exception because generally this exception indicates a programmer error,
 * and therefore would not be caught or handled in any way.
 *
 * @author James Ahlborn
 */
public class ValidationException extends RuntimeException
{
  private static final long serialVersionUID = -2933877497839744427L;  

  private transient Tuple2<ValidationContext,Verifiable> _failedVerifiable;
  
  public ValidationException(String message) {
    super(message);
  }
  
  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ValidationException(Throwable cause) {
    super(cause);
  }

  public Tuple2<ValidationContext,Verifiable> getFailedVerifiable() {
    return _failedVerifiable;
  }

  public void setFailedVerifiable(
      Tuple2<ValidationContext,Verifiable> newFailedVerifiable) {
    _failedVerifiable = newFailedVerifiable;
  }

  @Override
  public String getMessage() {
    String msg = super.getMessage();
    if(getFailedVerifiable() != null) {
      Verifiable verifiable = getFailedVerifiable().get1();
      try {
        msg = msg + " [Failed clause:" + verifiable + "]";
      } catch(Exception e) {
        msg = msg + " [Verifiable: " + verifiable.getClass().getName() + "@" +
          Integer.toHexString(System.identityHashCode(verifiable)) + "]";
      }
    }
    return msg;
  }
  
}

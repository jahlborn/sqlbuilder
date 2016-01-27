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

  private transient Tuple2<ValidationContext,? extends Verifiable<?>> _failedVerifiable;
  
  public ValidationException(String message) {
    super(message);
  }
  
  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ValidationException(Throwable cause) {
    super(cause);
  }

  public Tuple2<ValidationContext,? extends Verifiable<?>> getFailedVerifiable() {
    return _failedVerifiable;
  }

  public void setFailedVerifiable(
      Tuple2<ValidationContext,? extends Verifiable<?>> newFailedVerifiable) {
    _failedVerifiable = newFailedVerifiable;
  }

  @Override
  public String getMessage() {
    String msg = super.getMessage();
    if(getFailedVerifiable() != null) {
      Verifiable<?> verifiable = getFailedVerifiable().get1();
      try {
        msg = msg + " [Failed clause: " + verifiable + "]";
      } catch(Exception e) {
        msg = msg + " [Verifiable: " + verifiable.getClass().getName() + "@" +
          Integer.toHexString(System.identityHashCode(verifiable)) + "]";
      }
    }
    return msg;
  }
  
}

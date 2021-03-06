/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.common;

import org.apache.sqoop.classification.InterfaceAudience;
import org.apache.sqoop.classification.InterfaceStability;

@InterfaceAudience.Private
@InterfaceStability.Unstable
public final class SqoopProtocolConstants {
  public static final String HEADER_SQOOP_ERROR_CODE = "sqoop-error-code";

  public static final String HEADER_SQOOP_ERROR_MESSAGE = "sqoop-error-message";

  public static final String HEADER_SQOOP_INTERNAL_ERROR_CODE =
      "sqoop-internal-error-code";

  public static final String HEADER_SQOOP_INTERNAL_ERROR_MESSAGE =
      "sqoop-internal-error-message";

  public static final String charset = "UTF-8";

  public static final String JSON_CONTENT_TYPE =
      "application/json; charset=\"" + charset + "\"";

  private SqoopProtocolConstants() {
    // Disable explicit object creation
  }
}

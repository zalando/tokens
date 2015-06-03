/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.tokens;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AccessTokenEndpointException extends IllegalStateException {

    public static AccessTokenEndpointException from(HttpResponse response) {
        final StringBuilder message = new StringBuilder();
        message.append(response.getStatusLine().toString());

        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            message.append("\n").append("Response Body:").append("\n");
            try {
                message.append(EntityUtils.toString(entity));
            } catch (IOException e) {
                message.append("<<Error Reading Response Body>>");
            }
        }

        return new AccessTokenEndpointException(message.toString());
    }

    public AccessTokenEndpointException() {
    }

    public AccessTokenEndpointException(final String s) {
        super(s);
    }

    public AccessTokenEndpointException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccessTokenEndpointException(final Throwable cause) {
        super(cause);
    }
}

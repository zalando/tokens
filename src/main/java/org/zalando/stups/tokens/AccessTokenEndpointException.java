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

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

/**
 * "Model" interface specification for client credentials (as described on the OAuth2.0 spec).
 *
 * This might be any implementation from a static class that will provide the same <i>id</i> and
 * <i>secret</i> for all calls up to a {@link ThreadLocal} based implementation that can supply
 * different values for each {@link Thread}.
 */
public interface ClientCredentials {

    /**
     * Get the <i>id</i> for this {@link ClientCredentials}; this is called the <i>client_id</i> on
     * the OAuth2.0 spec.
     *
     * @return  A non null, not empty {@link String}
     */
    String getId();

    /**
     * Get the <i>secret</i> for this {@link ClientCredentials}; this is called the
     * <i>client_secret</i> on the OAuth2.0 spec.
     *
     * @return  A non null, not empty {@link String}
     */
    String getSecret();
}

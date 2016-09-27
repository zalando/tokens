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
 * "Model" interface specification for user credentials (as described on the OAuth2.0 spec).
 *
 * This might be any implementation from a static class that will provide the same <i>username</i>
 * and <i>password</i> for all calls up to a {@link ThreadLocal} based implementation that can
 * supply different values for each {@link Thread}. A {@link ThreadLocal} base implementation might
 * e.g. access data supplied with an incoming request when used in a request processing enviroment.
 */
public interface UserCredentials {

    /**
     * Get the <i>username</i> for this {@link UserCredentials}.
     *
     * @return  A non null, not empty {@link String}
     */
    String getUsername();

    /**
     * Get the <i>password</i> for this {@link UserCredentials}.
     *
     * @return  A non null, not empty {@link String}
     */
    String getPassword();
}

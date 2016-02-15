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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * #37
 * 
 * @author jbellmann
 *
 */
final class UserAgent {

    private static final String VERSION_TXT = "version.txt";

    private static final String UNKNOWN_VERSION = "unknown";

    static final String PREFIX = "stups-tokens";

    private final String agentName;

    private static final Logger LOG = LoggerFactory.getLogger(UserAgent.class);

    UserAgent() {
        String version = UNKNOWN_VERSION;
        try {
            ;
            Properties props = new Properties();
            props.load(UserAgent.class.getResourceAsStream(VERSION_TXT));
            version = props.getProperty("version", UNKNOWN_VERSION);
        } catch (Throwable t) {
            LOG.warn("Could not resolve 'VERSION' for user-agent");
        }

        this.agentName = PREFIX + "/" + version;
    }

    String get() {
        return agentName;
    }

}

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
package org.zalando.stups.tokens.util;

public class Metrics {

    private static final String DOT = ".";
    private static final String FAILURE = "failure";
    private static final String SUCCESS = "success";

    public static String buildMetricsKey(String prefix, boolean success) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(DOT).append(getSuffix(success));

        return sb.toString();
    }

    protected static String getSuffix(boolean success) {
        return success ? SUCCESS : FAILURE;
    }

}

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
package org.zalando.stups.tokens.fs;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

class AuthorizationHandler extends AccessTokenHandler {

    private final Map<String, Authorization> target;

    AuthorizationHandler(Map<String, Authorization> target) {
        // make parent happy
        super(new HashMap<>());
        this.target =requireNonNull(target, "'target' should not be null");
    }

    @Override
    public void accept(AccessTokenDto t) {
        this.target.put(t.getName(), new AuthorizationDto(t.getName(), t.getType(), t.getToken()));
    }

    @Override
    public boolean test(AccessTokenDto t) {
        return nonNull(t) && ("Bearer".equals(t.getType()) || "Basic".equals(t.getType()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public FilesystemReader getFilesystemReader() {
        return new FilesystemReader(this, this, this, super.get());
    }

}

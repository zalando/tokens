## Tokens

**Tokens** is a Java library for verifying and storing OAuth 2.0 service access tokens. It is resilient, configurable, and production-tested, and works with all JVM languages.

[![Build Status](https://travis-ci.org/zalando/tokens.svg?branch=master)](https://travis-ci.org/zalando/tokens)
[![Javadocs](http://www.javadoc.io/badge/org.zalando.stups/tokens.svg)](http://www.javadoc.io/doc/org.zalando.stups/tokens)
![Maven Central](https://img.shields.io/maven-central/v/org.zalando.stups/tokens.svg)
[![Coverage Status](https://coveralls.io/repos/zalando-stups/tokens/badge.svg?branch=master)](https://coveralls.io/r/zalando-stups/tokens?branch=master)
[![codecov.io](https://codecov.io/github/zalando/tokens/coverage.svg?branch=master)](https://codecov.io/github/zalando/tokens?branch=master)

### Project Features and Functionality

Some features Tokens offers:

- Support for credential rotation, by reading them on-demand from the file system
- Extensibility with a credentials provider
- Configuration flexibility; specify multiple tokens with different scopes
- Ability to inject fixed OAuth2 access tokens

Tokens can be useful to devs (at any company, large or small) who are working with highly-distributed microservices deployed in the cloud and need to authenticate the traffic generated when accessing APIs. For example, if your team wants to consume an API with OAuth2 credentials, Tokens will fetch the tokens for you. Then you just [add scopes](#Usage) in the token. 

When creating tokens, it's easy to make a lot of mistakes. Tokens aims to save you hassle and time.

### Prerequisites

- Java 11
- Maven
- Gradle

#### Maven Dependency

Add it with:

```xml
<dependency>
    <groupId>org.zalando.stups</groupId>
    <artifactId>tokens</artifactId>
    <version>see above</version>
</dependency>
```

#### Gradle Dependency

``compile('org.zalando.stups:tokens:${version}') ``

### Usage in Zalando K8s environment (with `PlatformCredentialsSet`)

It uses `/meta/credentials` as a default folder to look for provided tokens by `PlatformCredentialsSet`.

```java
import org.zalando.stups.tokens.Tokens;
import org.zalando.stups.tokens.AccessTokens;

AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://this.url.will.be.ignored"))
                            .start();

while (true) {
    final String token = tokens.get("exampleRO");

    Request.Get("https://api.example.com")
           .addHeader("Authorization", "Bearer " + token)
           .execute():

    Thread.sleep(1000);
}
```

Want to migrate from STUPS to K8s? [See the hints](#migration-from-zalandos-stups-env-to-zalandos-k8s-env).

### Usage in Zalandos STUPS environment

```java
import org.zalando.stups.tokens.Tokens;
import org.zalando.stups.tokens.AccessTokens;

AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://example.com/access_tokens"))
                            .manageToken("exampleRW")
                                .addScope("read")
                                .addScope("write")
                                .done()
                            .manageToken("exampleRO")
                                .addScope("read")
                                .done()
                            .start();

while (true) {
    final String token = tokens.get("exampleRO");

    Request.Get("https://api.example.com")
           .addHeader("Authorization", "Bearer " + token)
           .execute():

    Thread.sleep(1000);
}
```

### Migration from Zalandos STUPS env to Zalandos K8s env

Your code can stay as is.

A common issue is not mounting the credentials. Please use the example below as a guide line.

```
...
          volumeMounts:
          - name: "{{ APPLICATION }}-credentials"
            mountPath: /meta/credentials
            readOnly: true
      volumes:
        - name: "{{ APPLICATION }}-credentials"
          secret:
            secretName: "{{ APPLICATION }}-credentials"
```

Please also make sure that token identifiers/names must equal the respective items in `credentials.yaml`::

```
apiVersion: "zalando.org/v1"
kind: PlatformCredentialsSet
metadata:
   name: "{{ APPLICATION }}-credentials"
spec:
   application: "{{ APPLICATION }}"
   tokens:
     exampleRW:
       privileges:
         - com.zalando::read
         - com.zalando::write
     exampleRO:
       privileges:
         - com.zalando::read
```

### Local Testing

With Tokens, you can inject fixed OAuth2 access tokens via the `OAUTH2_ACCESS_TOKENS` environment variable and test applications locally with personal OAuth2 tokens. As an example:

```bash
$ MY_TOKEN_1=$(zign token -n mytok1)
$ MY_TOKEN_2=$(zign token -n mytok2)
$ export OAUTH2_ACCESS_TOKENS=mytok1=$MY_TOKEN_1,mytok2=$MY_TOKEN_2
$ lein repl # start my local Clojure app using the tokens library
```

In production on EC2 instances, Tokens fetches access tokens by requesting an authorization server with credentials, found in `client.json` and `user.json`. It's also possible to provide `client.json` and `user.json` with valid content and point this library to that directory.

### Contributing

This project welcomes contributions, including bug fixes and documentation enhancements. To contribute, please use the Issues Tracker to let us know what you would like to do. We'll respond, and go from there.

### License

Copyright © 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

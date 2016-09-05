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
 * Exception class indicating that {@link AccessTokens} implementation is not able to provide an
 * access token for the specified <i>tokenId</i>
 */
@SuppressWarnings("serial")
public class AccessTokenUnavailableException extends IllegalStateException {

	/**
	 * Create a simple instance without any additional information.
	 *
	 * You should not use this constructor if possible but use one of the more descriptive ones
	 * instead
	 */
	public AccessTokenUnavailableException() {
		super();
	}

	/**
	 * Create a simple instance with the specified message
	 *
	 * @param s  The exception message to use
   */
	public AccessTokenUnavailableException(final String s) {
		super(s);
	}

	/**
	 * Create a complex instance with a cause included as well as the specified message
	 *
	 * @param message  The exception message to use
	 * @param cause    The cause {@link Throwable} for this exception
   */
	public AccessTokenUnavailableException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a complex instance with a cause included.
	 *
	 * You should use the constructor that includes a message instead!
	 *
	 * @param cause  The case {@link Throwable} for this exception
   */
	public AccessTokenUnavailableException(final Throwable cause) {
		super(cause);
	}
}

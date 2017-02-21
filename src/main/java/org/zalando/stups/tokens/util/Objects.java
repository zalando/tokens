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

import java.util.Collection;

import org.apache.http.util.Args;

/**
 * Basic Utilities. We know there are a lot of libs out there.
 * We just delegate to one in classpath here not to spread around the
 * dependency.
 * 
 * @author jbellmann
 *
 */
public class Objects {

	private Objects() {
		// hide
	}

	public static <T> T notNull(String name, T object) {
		return Args.notNull(object, name);
	}

	public static <T extends CharSequence> T notBlank(String name, T argument) {
		return Args.notBlank(argument, name);
	}

	public static <T extends Object> Collection<T> noNullEntries(String name, Collection<T> collection) {
		name = notBlank("name", name);
		collection = notNull(name, collection);
		try {
			if (collection.contains(null)) {
				throw new IllegalArgumentException(name + " should not contain 'null'");
			}
		} catch (NullPointerException e) {
			return collection;
		}
		return collection;
	}

	public static <T extends CharSequence> Collection<T> noBlankEntries(String name, Collection<T> collection) {
		name = notBlank("name", name);
		collection = notNull(name, collection);
		try {
			if (collection.contains(null)) {
				throw new IllegalArgumentException(name + " should not contain 'null'");
			}
			try {
				for (T element : collection) {
					Args.notBlank(element, name);
				}
			} catch (IllegalArgumentException ignore) {
				throw new IllegalArgumentException(name + " should not contain blank elements");
			}
		} catch (NullPointerException e) {
			return collection;
		}

		return collection;
	}

}

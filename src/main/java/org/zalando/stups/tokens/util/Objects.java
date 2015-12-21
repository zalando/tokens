package org.zalando.stups.tokens.util;

import org.apache.http.util.Args;

/**
 * Basic Utilities. We know there are a lot of libs out there. <br />
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

}

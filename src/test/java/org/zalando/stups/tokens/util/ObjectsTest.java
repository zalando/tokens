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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class ObjectsTest {

	@Test(expected = IllegalArgumentException.class)
	public void avoidNullInCollection() {
		List<String> stringList = new ArrayList<>();
		stringList.add(null);
		stringList.add("TEST");
		Objects.noNullEntries("argument", stringList);
	}

	@Test(expected = IllegalArgumentException.class)
	public void avoidNullInSet() {
		Set<String> set = new HashSet<>();
		set.add(null);
		set.add("TEST");
		Objects.noNullEntries("argument", set);
	}

	@Test
	public void avoidNullInEnumSet() {
		EnumSet<Status> enumSet = EnumSet.of(Status.ACTIVE, Status.DEACTIVATED);
		// you are not able to add 'null' to a EnumSet,
		// but 'contains(null)' causes no NullPointerException
		// enumSet.add(null);
		// see TreeSet below for difference
		Objects.noNullEntries("argument", enumSet);
	}

	@Test
	public void avoidNullInTreeSet() {
		TreeSet<String> treeSet = new TreeSet<>();
		treeSet.add("test");
		treeSet.add("test2");
		// you are not able to add 'null' to a TreeSet,
		// but 'contains(null)' throws a NullPointerException
		// treeSet.add(null);
		// see EnumSet for different behavior
		Objects.noNullEntries("argument", treeSet);
	}

	enum Status {
		ACTIVE, DEACTIVATED;
	}
}

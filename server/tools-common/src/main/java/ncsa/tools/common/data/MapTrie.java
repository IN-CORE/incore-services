/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.data;

import java.util.Map;
import java.util.TreeMap;

import ncsa.tools.common.util.SystemUtils;

public class MapTrie
{
	private static final String END_OF_WORD = "NULL";

	private Map root;

	public MapTrie()
	{
		root = new TreeMap();
	}

	public void add(String key)
	{
		if (key == null || key.equals(""))
			return;
		Map current = root;
		int c = 0;
		for (; c < key.length(); c++) {
			String cstr = key.substring(c, c + 1);
			Map m = (Map) current.get(cstr);
			if (m == null) {
				m = new TreeMap();
				current.put(cstr, m);
			}
			current = m;
		}
		current.put(END_OF_WORD, null);
	}

	public boolean contains(String key)
	{
		if (key == null || key.equals(""))
			return false;
		Map current = root;
		int c = 0;
		for (; c < key.length(); c++) {
			String cstr = key.substring(c, c + 1);
			Map m = (Map) current.get(cstr);
			if (m == null)
				return false;
			current = m;
		}
		return current.containsKey(END_OF_WORD);
	}

	public Map first(String charStr)
	{
		return next(charStr, root);
	}

	public static Map next(String charStr, Map current)
	{
		return (Map) current.get(charStr);
	}

	public static boolean isKey(Map current)
	{
		return current.containsKey(END_OF_WORD);
	}

	public void print()
	{
		SystemUtils.print(root, false);
	}
}

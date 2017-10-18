/*******************************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Contributors:
 *     Shawn Hampton, Jong Lee, Chris Navarro, Nathan Tolbert (NCSA) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package edu.illinois.ncsa.incore.services.fragility.mapping;

import ncsa.tools.common.UserFacing;
import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.types.filters.MatchFilter;
import ncsa.tools.common.util.XmlUtils;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 
 *
 *        TODO add class documentation and license header
 */
public class MatchFilterMap implements UserFacing
{
	public final static String TAG_SELF = "match-filter-map"; //$NON-NLS-1$

	private List<PropertyMatch> matches = new LinkedList<>();

	public MatchFilterMap()
	{
	}

	public MatchFilterMap(Element element)
	{
		initializeFromElement(element);
	}

	public List<PropertyMatch> getPropertyMatches()
	{
		return matches;
	}

	public void addPropertyMatch(PropertyMatch match)
	{
		matches.add(match);
	}

	public void addPropertyMatch(String key, MatchFilter matchFilter)
	{
		matches.add(new PropertyMatch(key, matchFilter));
	}

	public Element asElement()
	{
		Element element = new DefaultElement(TAG_SELF);

		for (PropertyMatch match : matches) {
			element.add(match.asElement());
		}

		return element;
	}

	public void initializeFromElement(Element element)
	{
		matches.clear();

		Iterator<?> iterator = element.elementIterator(PropertyMatch.TAG_SELF);

		while (iterator.hasNext()) {
			matches.add(new PropertyMatch((Element) iterator.next()));
		}
	}

    public static MatchFilterMap loadMatchFilterMapFromUrl(String mappingUrl) throws DeserializationException {
        try {
            return loadMatchFilterMapFromUrl(new URL(mappingUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        throw new DeserializationException("Could not deserialize " + mappingUrl);
    }

    public static MatchFilterMap loadMatchFilterMapFromUrl(URL mappingUrl) throws DeserializationException {
        try {
            MappingDatasetStub stub = new MappingDatasetStub();
            XmlUtils.deserializeUserFacingBeanFromFile(mappingUrl, stub);
            return stub.getMatchFilterMap();
        } catch (DeserializationException e) {
            e.printStackTrace();
        }

        throw new DeserializationException("Could not deserialize " + mappingUrl.toString());
    }
}

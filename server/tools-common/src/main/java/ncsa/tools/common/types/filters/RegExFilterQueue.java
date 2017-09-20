package ncsa.tools.common.types.filters;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.util.ExceptionUtils;

/**
 * Three types of queue configuration are available:
 * <P>
 * <table>
 * <tr>
 * <td></td>
 * <td>ONE_TO_ONE</td>
 * <td>at most one match per character sequence (no overlapping); action is determined by the first filter matched on the sequence;</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>PRIORITY</td>
 * <td>same as ONE_TO_ONE; a priority queue based on the running count of matches is maintained;</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>MANY_TO_ONE</td>
 * <td>multiple and overlapping actions are enabled; each filter in the queue is applied on every intercepted character sequence.</td>
 * </tr>
 * </table>
 * 
 * @see RegExFilter
 * 
 * @author Albert L. Rossi
 */
public class RegExFilterQueue
{
	private static Logger logger = Logger.getLogger(RegExFilterQueue.class);

	private LinkedList list = new LinkedList();
	private int queueType = NCSAConstants.ONE_TO_ONE;
	private RegExFilter lookingFor = null;

	/**
	 * @param i
	 *            options: one of the OgreConstants ONE_TO_ONE, PRIORITY or
	 *            MANY_TO_ONE.
	 */
	public void setTypeValue(int i)
	{
		queueType = i;
	}

	/**
	 * @param s
	 *            options: "one-to-one" (default),
	 *            "priority", "many-to-one".
	 * 
	 * @throws NCSAException
	 *             if type is not one of the above options.
	 * 
	 */
	public void setType(String s) throws IllegalArgumentException
	{
		if (s.equalsIgnoreCase("one-to-one"))
			queueType = NCSAConstants.ONE_TO_ONE;
		else if (s.equalsIgnoreCase("priority"))
			queueType = NCSAConstants.PRIORITY;
		else if (s.equalsIgnoreCase("many-to-one"))
			queueType = NCSAConstants.MANY_TO_ONE;
		else
			throw new IllegalArgumentException("unrecognized queue type: " + s + "; must be either 'one-to-one', "
					+ "'priority' or 'many-to-one'");
	} // setType

	/**
	 * @return NCSAConstant integer value.
	 */
	public int getType()
	{
		return queueType;
	}

	/**
	 * @return "one-to-one", "priority" or "many-to-one".
	 * 
	 */
	public String getTypeString()
	{
		switch (queueType) {
		case NCSAConstants.PRIORITY:
			return "priority";
		case NCSAConstants.MANY_TO_ONE:
			return "many-to-one";
		case NCSAConstants.ONE_TO_ONE:
		default:
			return "one-to-one";
		}
	} // getType

	/**
	 * @param filter
	 *            to be processed (in order).
	 * 
	 */
	public void addRegexFilter(RegExFilter filter)
	{
		list.addLast(filter);
	}

	/**
	 * Main routine: loops through the filters and
	 * tries to match against them. Any and all matched filters
	 * are returned as a List (which will contain at most one
	 * filter for ONE_TO_ONE or PRIORITY queue types).
	 * 
	 * @param charSequence
	 *            the stream sequence to check for match.
	 */
	public List applyFilters(String charSequence)
	{
		List matches = new ArrayList();
		RegExFilter filter = null;
		int filterState = NCSAConstants.UNMATCHED;
		int index = -1;

		/*
		 * not MANY_TO_ONE; a partial match has previously been found
		 * on this filter, so we should only attempt to find its
		 * closing tag
		 */
		if (lookingFor != null) {
			filterState = lookingFor.apply(charSequence);
			if (filterState == NCSAConstants.MATCHED) {
				matches.add(lookingFor);
				if (queueType == NCSAConstants.PRIORITY) {
					index = list.indexOf(lookingFor);
					if (index > 0)
						updateQueue(index);
				}
				lookingFor = null;
			}
			return matches;
		}

		/*
		 * check the queue for matches
		 * in case of MANY_TO_ONE, apply each filter
		 * add all those with state MATCHED to the list
		 * otherwise, look for the first result != UNMATCHED
		 * if it is LOOKING, assign it to lookingFor
		 * otherwise add it to the list
		 * if MATCH && PRIORITY, update the queue
		 */
		try {
			ListIterator lit = list.listIterator(0);
			while (lit.hasNext()) {
				index++;
				filter = (RegExFilter) lit.next();
				filterState = filter.apply(charSequence);
				if (filterState == NCSAConstants.MATCHED) {
					matches.add(filter);
					if (queueType == NCSAConstants.PRIORITY && index > 0)
						updateQueue(index);
					if (queueType != NCSAConstants.MANY_TO_ONE) {
						lookingFor = null;
						break;
					}
				}
				if (queueType == NCSAConstants.MANY_TO_ONE)
					continue;
				if (filterState == NCSAConstants.LOOKING) {
					lookingFor = filter;
					break;
				}
			}
		} catch (ConcurrentModificationException cme) {
			// this should not happen!
			// this list is not meant to be thread-reusable!
			logger.error(ExceptionUtils.getStackTrace(cme));
		} catch (IndexOutOfBoundsException iobe) {
			// neither should this!
			logger.error(ExceptionUtils.getStackTrace(iobe));
		} catch (ClassCastException cce) {
			logger.error(ExceptionUtils.getStackTrace(cce));
		}

		return matches;
	} // applyFilter

	/**
	 * Resets all filter match counts in the queue to 0.
	 */
	public void resetCounts()
	{
		RegExFilter filter = null;
		try {
			ListIterator lit = list.listIterator(0);
			while (lit.hasNext()) {
				filter = (RegExFilter) lit.next();
				filter.resetCount();
			}
		} catch (ConcurrentModificationException cme) {
			// this should not happen!
			// this list is not meant to be thread-reusable!
			logger.error(ExceptionUtils.getStackTrace(cme));
		} catch (IndexOutOfBoundsException iobe) {
			// neither should this!
			logger.error(ExceptionUtils.getStackTrace(iobe));
		}
	} // resetCounts

	/**
	 * Resets all filter states in the queue.
	 */
	public void resetState()
	{
		RegExFilter filter = null;
		try {
			ListIterator lit = list.listIterator(0);
			while (lit.hasNext()) {
				filter = (RegExFilter) lit.next();
				filter.resetState();
			}
		} catch (ConcurrentModificationException cme) {
			// this should not happen!
			// this list is not meant to be thread-reusable!
			logger.error(ExceptionUtils.getStackTrace(cme));
		} catch (IndexOutOfBoundsException iobe) {
			// neither should this!
			logger.error(ExceptionUtils.getStackTrace(iobe));
		}
	} // resetState

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * AUXILIARY METHODS //
	 * /////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Removes the filter at the indicated index and re-inserts
	 * it at an updated position which maintains the sorted descending
	 * order of match counts.
	 * 
	 * @param p_currIndex
	 *            current index of the filter to be moved
	 */
	private void updateQueue(int p_currIndex)
	{
		RegExFilter toBeMoved = (RegExFilter) list.remove(p_currIndex);
		long toBeMovedCount = toBeMoved.getCount();

		int newIndex = 0;
		RegExFilter element = null;

		/*
		 * loop from the beginning of the queue, comparing counts;
		 * break when the element's count >= this filter's count
		 */
		try {
			ListIterator lit = list.listIterator(0);
			while (lit.hasNext()) {
				element = (RegExFilter) lit.next();
				if ((element.getCount() < toBeMovedCount) || (element.getCount() == toBeMovedCount))
					break;
				newIndex++;
			}
		} catch (ConcurrentModificationException cme) {
			// this should not happen!
			// this list is not meant to be thread-reusable!
			logger.error(ExceptionUtils.getStackTrace(cme));
			logger.error("updateQueue: restoring to original index");
			list.add(p_currIndex - 1, toBeMoved);
		} catch (IndexOutOfBoundsException iobe) {
			// neither should this!
			logger.error(ExceptionUtils.getStackTrace(iobe));
			logger.error("updateQueue: restoring to original index");
			list.add(p_currIndex - 1, toBeMoved);
		}

		if (newIndex > list.size()) {
			/*
			 * because of the invariant total ordering on the queue,
			 * this condition should only arise when the matched
			 * filter is already the last one
			 */
			list.addLast(toBeMoved);
			logger.debug("updateQueue: added filter [count = " + toBeMovedCount + "] to end of queue");
		} else {
			list.add(newIndex, toBeMoved);
			logger.debug("updateQueue: inserted filter [count = " + toBeMovedCount + "] before element " + newIndex);
		}
	} // updateQueue

}

package ncsa.tools.common.concurrent;

import java.util.HashMap;
import java.util.Map;

public class PinTable
{
	private Map pinned = new HashMap();

	public void pin(String key)
	{
		if (key == null)
			throw new IllegalArgumentException("key was null");
		synchronized (pinned) {
			while (pinned.containsKey(key)) {
				try {
					pinned.wait();
				} catch (InterruptedException ignored) {
				}
			}
			pinned.put(key, null);
		}
	}

	public void unpin(String key)
	{
		if (key == null)
			throw new IllegalArgumentException("key was null");
		synchronized (pinned) {
			pinned.remove(key);
			pinned.notifyAll();
		}
	}
}

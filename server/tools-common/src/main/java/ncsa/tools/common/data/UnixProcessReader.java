/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.data;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ncsa.tools.common.util.SystemUtils;

public class UnixProcessReader {
    private String user;
    private String tagToMatch;
    private String root;
    private String remotePrefix;
    private String signal;

    // STATE
    private List pids;
    private Map ppids;

    public UnixProcessReader() throws IllegalStateException {
        if (!SystemUtils.isUNIX())
            throw new IllegalStateException("cannot use UnixProcessReader on this platform");
        ppids = new HashMap();
        pids = new ArrayList();
    }

    public void retrieveProcessIds() throws Throwable {
        getProcessLines();
        findAll();
    }

    public void killAll() throws Throwable {
        retrieveProcessIds();
        Process pd = null;
        try {
            pd = Runtime.getRuntime().exec(getKillCommand());
            pd.waitFor();
        } catch (InterruptedException ignored) {
        } finally {
            SystemUtils.destroy(pd, null);
        }
    }

    protected void getProcessLines() throws Throwable {
        Process pd = null;
        Thread[] streams = new Thread[2];
        try {
            pd = Runtime.getRuntime().exec(getPsCommand());
            streams[1] = SystemUtils.streamConsumer(new InputStreamReader(pd.getErrorStream()), true);
            streams[1].start();
            streams[0] = getStdOut(pd);
            pd.waitFor();
        } catch (InterruptedException ignored) {
        } finally {
            SystemUtils.destroy(pd, streams);
        }
    }

    protected String getPsCommand() {
        StringBuffer command = new StringBuffer();
        if (remotePrefix != null)
            command.append(remotePrefix).append(" ");
        command.append("/bin/ps");
        if (SystemUtils.isMAC())
            command.append(" -Ao user,pid,ppid,command");
        else
            command.append(" -eo user,pid,ppid,command");
        return command.toString();
    }

    protected String getKillCommand() {
        StringBuffer command = new StringBuffer();
        if (remotePrefix != null)
            command.append(remotePrefix).append(" ");
        command.append("kill ");
        if (signal != null)
            command.append(signal);
        else
            command.append(-9);
        for (Iterator it = pids.iterator(); it.hasNext(); ) {
            command.append(" ");
            command.append(it.next());
        }
        return command.toString();
    }

    protected Thread getStdOut(Process pd) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(pd.getInputStream()));
        ppids.clear();
        int len = tagToMatch == null ? 0 : tagToMatch.length();
        len = Math.min(50, len);
        final String key = len == 0 ? null : tagToMatch.substring(0, len);
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    String line = null;
                    try {
                        line = reader.readLine();
                    } catch (EOFException eofe) {
                        break;
                    } catch (IOException ioe) {
                        break;
                    }
                    if (line == null)
                        break;
                    if (user == null || line.indexOf(user) >= 0)
                        addLine(line);
                    if (root == null && key != null && line.indexOf(key) >= 0) {
                        String[] parts = StringUtils.split(line);
                        root = parts[1];
                    }
                }

                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        };
        t.start();
        return t;
    }

    protected void addLine(String line) {
        String[] parts = StringUtils.split(line);
        String pid = parts[1];
        String ppid = parts[2];
        List children = (List) ppids.get(ppid);
        if (children == null) {
            children = new ArrayList();
            ppids.put(ppid, children);
        }
        children.add(pid);
        if (!ppids.containsKey(pid))
            ppids.put(pid, null);
    }

    protected void findAll() {
        pids.clear();
        if (root == null) {
            pids.add("" + -1);
            return;
        }
        List q = new ArrayList();
        q.add(root);
        while (!q.isEmpty()) {
            String next = (String) q.remove(0);
            pids.add(next);
            List children = (List) ppids.get(next);
            if (children != null)
                q.addAll(children);
        }
    }

    // BEAN METHODS

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getTagToMatch() {
        return tagToMatch;
    }

    public void setTagToMatch(String tagToMatch) {
        this.tagToMatch = tagToMatch;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List getPids() {
        return pids;
    }

    public String getRemotePrefix() {
        return remotePrefix;
    }

    public void setRemotePrefix(String remotePrefix) {
        this.remotePrefix = remotePrefix;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }
}

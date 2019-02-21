/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.concurrent.ExceptionPreservingThread;
import ncsa.tools.common.data.UnixProcessReader;
import ncsa.tools.common.types.Profile;
import ncsa.tools.common.types.Property;

/**
 * Utility methods for System-related functionality.
 * 
 * @author Albert L. Rossi
 */
public class SystemUtils
{
	private static Logger logger = Logger.getLogger(SystemUtils.class);

	public static final Random random = new Random(System.currentTimeMillis());

	/**
	 * Three-member array: 0 = host, 1 = IP, 2 = OS.
	 */
	public static String[] uname = null;

	static {
		List info = new ArrayList();
		uname("nis", info);
		uname = (String[]) info.toArray(new String[0]);
	} // static initializer

	/**
	 * Static utility class; cannot be constructed.
	 */
	private SystemUtils()
	{
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * THREADING //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Safety feature: checks to make sure the calling thread is not the thread
	 * on which the join is called, to avoid deadlock.
	 * 
	 * @param thread
	 *            to join on.
	 */
	public static void join(Thread thread)
	{
		if (thread == null || Thread.currentThread() == thread)
			return;
		try {
			thread.join();
		} catch (InterruptedException ignored) {
		}
	} // join

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * ENVIRONMENT & PROPERTIES //
	 * /////////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Replaces occurrences of $NAME in arg with value if it is in the passed-in
	 * environment. Restriction: an arg can be either a variable name, or prefix
	 * + variable name (e.g., "$HOME", "-np $NUMBER_OF_PROCS", but not "$Vy" or
	 * "x$Vy"; more than one name in an arg is not contemplated either; eg.
	 * "$A$B").
	 * 
	 * @param args
	 *            the arguments conforming to the restrictions above;
	 * @param env
	 *            definitions of the type "A=B";
	 * @param failFast
	 *            if true and an undefined variable name is enountered, will throw
	 *            the exception; else will substitute a zero-length string for the
	 *            name.
	 */
	public static void replaceEnvVarNames(String[] args, String[] env, boolean failFast) throws IllegalArgumentException
	{
		if (args == null || env == null)
			return;
		Map envVars = getEnvironmentVariableMap(env);
		for (int i = 0; i < args.length; i++) {
			int j = args[i].indexOf("$");
			if (j >= 0) {
				int k = j + 1;
				if (k == args[i].length())
					k = j;
				String head = args[i].substring(0, j);
				String name = args[i].substring(k);
				String tail = (String) envVars.get(name);
				if (tail == null) {
					if (failFast)
						throw new IllegalArgumentException("replaceEnvVarNames, " + name + " is undefined in the current environment: "
								+ envVars);
					tail = "";
				}
				args[i] = head + tail;
			}
		}
	}

	/**
	 * Returns the environment as array of name=value string.
	 * 
	 * @param profile
	 *            object with extra environment variable definitions; overrides
	 *            current environment; can be <code>null</code>.
	 */
	public static String[] getEnvironment(Profile profile)
	{
		Map map = new HashMap();

		List currEnv = SystemUtils.envToProperties(false);

		for (int i = 0; i < currEnv.size(); i++) {
			String var = (String) currEnv.get(i);
			int index = var.indexOf("=");
			if (index > 0) {
				String key = var.substring(0, var.indexOf("="));
				map.put(key, var);
			} else {
				map.put(var, "");
			}
		}

		if (profile != null) {
			List penv = profile.getAllPropertiesOfCategory("environment");
			for (Iterator i = penv.iterator(); i.hasNext();) {
				Property p = (Property) i.next();
				String name = p.getName();
				String value = p.getValue();
				if (value == null)
					value = "";
				else
					value = name + "=" + value;
				map.put(name, value);
			}
		}

		return (String[]) map.values().toArray(new String[0]);
	}

	/**
	 * Calls envToProperties with parameter = 'true'.
	 */
	public static List envToProperties()
	{
		return envToProperties(true);
	} // envToProperties

	/**
	 * Runs "env", piping its output to a thread which reads it into a buffer;
	 * when the thread exits, processEnv is called to parse the string.
	 * 
	 * @param b
	 *            if true, sets System properties.
	 * @return list of environment mappings, i.e., name=value.
	 * @see #processEnv
	 */
	public static List envToProperties(boolean b)
	{
		Runtime rt = Runtime.getRuntime();
		Process p = null;
		String[] command = null;
		StringBuffer envBuffer = new StringBuffer();

		File f = new File("/bin/env");

		if (!f.exists()) {
			f = new File("/usr/bin/env");
			if (!f.exists())
				return new ArrayList();
		}

		command = new String[] { f.getAbsolutePath() };

		try {
			p = rt.exec(command);
		} catch (IOException ioe) {
			if (p != null) {
				p.destroy();
				p = null;
			}
		} catch (SecurityException se) {
			if (p != null) {
				p.destroy();
				p = null;
			}
		}

		if (p == null)
			return null;

		join(getEnv(p, envBuffer));

		p.destroy();

		return processEnv(envBuffer, b);
	} // envToProperties

	/**
	 * @param vars
	 *            definitions of the type "A=B";
	 * @return map of corresponding entries (A, B).
	 */
	public static Map getEnvironmentVariableMap(String[] vars)
	{
		Map map = new HashMap();
		if (vars != null) {
			for (int i = 0; i < vars.length; i++) {
				String[] nvPair = vars[i].split("=");
				String name = null;
				String value = null;
				if (nvPair.length > 0) {
					name = nvPair[0];
				}
				if (nvPair.length > 1) {
					value = nvPair[1];
				}
				if (name != null)
					map.put(name, value);
			}
		}
		return map;
	}

	/**
	 * @param nvpairs
	 *            map of name value pairs.
	 * @return list of name=value.
	 */
	public static List getEnvironmentList(Map nvpairs)
	{
		List env = new ArrayList();
		for (Iterator it = nvpairs.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			env.add(entry.getKey() + "=" + entry.getValue());
		}
		return env;
	}

	/**
	 * Turns the System properties into a "name: value" list. If sort is true,
	 * sorts the name keys first.
	 * 
	 * @param sorted
	 *            if true, keys are in sorted order.
	 * @return single string of property definitions.
	 */
	public static String propertiesToList(boolean sorted)
	{
		Properties properties = System.getProperties();
		Set keySet = properties.keySet();
		int numKeys = keySet.size();
		String[] keys = new String[numKeys];
		Iterator keyIter = keySet.iterator();
		int index = 0;
		while (keyIter.hasNext())
			keys[index++] = (String) keyIter.next();
		if (sorted)
			Arrays.sort(keys);
		StringBuffer buffer = new StringBuffer(0);
		for (int i = 0; i < numKeys; i++) {
			buffer.append(keys[i]);
			buffer.append(":\t");
			buffer.append(properties.getProperty(keys[i]));
			buffer.append(NCSAConstants.LINE_SEP);
		}
		return buffer.toString();
	} // propertyList

	/**
	 * @param var
	 *            name of property.
	 * @return true if System properties contains var.
	 */
	public static boolean isDefined(String var)
	{
		return (System.getProperty(var) != null);
	}

	/**
	 * @param vars
	 *            array of property names.
	 * @return true if System properties contain all of vars; else false.
	 */
	public static boolean areDefined(String[] vars)
	{
		for (int i = 0; i < vars.length; i++)
			if (!isDefined(vars[i]))
				return false;
		return true;
	} // areDefined

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * FINDWORKINGDIR //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Category of property can be "environment", "property", or <code>null</code>
	 * 
	 * @param properties
	 *            ncsa.common.types.Property list to check (in order).
	 * @return path of working directory, or <code>null</code> if no definition
	 *         is found.
	 */
	public static String findWorkingDir(List properties)
	{
		if (properties == null)
			return null;
		return findWorkingDir((Property[]) properties.toArray(new Property[0]));
	}

	/**
	 * Category of property can be "environment", "property", or <code>null</code>
	 * 
	 * @param properties
	 *            to check (in order).
	 * @return path of working directory, or <code>null</code> if no definition
	 *         is found.
	 */
	public static String findWorkingDir(Property[] properties)
	{
		if (properties == null)
			return null;
		Hashtable sysprops = System.getProperties();
		Hashtable envVars = ListUtils.listToProperties(SystemUtils.envToProperties(false));
		String dir = null;

		for (int i = 0; i < properties.length; i++) {
			String category = properties[i].getCategory();
			boolean sys = false;
			boolean env = false;
			if (category == null) {
				sys = true;
				env = true;
			} else if (category.equalsIgnoreCase("environment")) {
				env = true;
			} else if (category.equalsIgnoreCase("property")) {
				sys = true;
			}
			if (env)
				dir = (String) envVars.get(properties[i].getName());
			if (dir == null && sys)
				dir = (String) sysprops.get(properties[i].getName());
			if (dir != null)
				break;
		}

		return dir;
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * UNAME //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @param options
	 *            non-separated sequence of the following characters:
	 *            <P>
	 *            <P>
	 *            a [= architecture];
	 *            <P>
	 *            s [= operating system];
	 *            <P>
	 *            v [= version];
	 *            <P>
	 *            n [= node name];
	 *            <P>
	 *            i [= IP address];
	 *            <P>
	 *            l [= include labels in output].
	 * @param parsed
	 *            list for recording parsed information (can be null).
	 * @return information as a string; if labels are indicated, the results are
	 *         line-separated; else they are white-space-separated.
	 */
	public static String uname(String options, List parsed)
	{
		boolean arch = false;
		boolean node = false;
		boolean os = false;
		boolean version = false;
		boolean ipAddress = false;
		boolean labels = false;

		if (options == null || options.equals(""))
			return null;

		char[] tags = options.toCharArray();

		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == 'a')
				arch = true;
			else if (tags[i] == 's')
				os = true;
			else if (tags[i] == 'v')
				version = true;
			else if (tags[i] == 'n')
				node = true;
			else if (tags[i] == 'i')
				ipAddress = true;
			else if (tags[i] == 'l')
				labels = true;
		}
		StringBuffer info = new StringBuffer();
		InetAddress address = null;
		String nodeName = null;
		String ipString = null;
		String property = null;

		try {
			address = InetAddress.getLocalHost();
			nodeName = address.getHostName();
			ipString = address.getHostAddress();
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
			nodeName = "unknown";
			ipString = "unknown";
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			nodeName = "unknown";
			ipString = "unknown";
		}

		if (node) {
			if (labels)
				info.append("name\t");
			info.append(nodeName);
			if (parsed != null)
				parsed.add(nodeName);
			if (labels)
				info.append(NCSAConstants.LINE_SEP);
		}

		if (ipAddress) {
			if (labels)
				info.append("address\t");
			else if (info.length() > 0)
				info.append(" ");
			info.append(ipString);
			if (parsed != null)
				parsed.add(ipString);
			if (labels)
				info.append(NCSAConstants.LINE_SEP);
			else
				info.append(" ");
		}

		if (arch) {
			property = System.getProperty("os.arch");
			if (labels)
				info.append("arch\t");
			else if (info.length() > 0)
				info.append(" ");
			info.append(property);
			if (parsed != null)
				parsed.add(property);
			if (labels)
				info.append(NCSAConstants.LINE_SEP);
		}

		if (os) {
			property = System.getProperty("os.name");
			if (labels)
				info.append("os\t");
			else if (info.length() > 0)
				info.append(" ");
			info.append(property);
			if (parsed != null)
				parsed.add(property);
			if (labels)
				info.append(NCSAConstants.LINE_SEP);
			else
				info.append(" ");
		}

		if (version) {
			property = System.getProperty("os.version");
			if (labels)
				info.append("version\t");
			else if (info.length() > 0)
				info.append(" ");
			info.append(property);
			if (parsed != null)
				parsed.add(property);
			if (labels)
				info.append(NCSAConstants.LINE_SEP);
		}
		return info.toString();
	} // uname

	/**
	 * @return true if the local platform (OS) is UNIX-like.
	 */
	public static boolean isUNIX()
	{
		return uname[2].equalsIgnoreCase("UNIX") || uname[2].equalsIgnoreCase("Linux") || uname[2].equalsIgnoreCase("AIX")
				|| uname[2].equalsIgnoreCase("HPUX") || uname[2].equalsIgnoreCase("IRIX") || isMAC();
	} // isUNIX

	public static boolean isMAC()
	{
		return uname[2].equalsIgnoreCase("Mac OS X") || uname[2].equalsIgnoreCase("Darwin") || uname[2].equalsIgnoreCase("Tiger")
				|| uname[2].equalsIgnoreCase("Panther") || uname[2].equalsIgnoreCase("Leopard");
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * CHMOD //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Makes the file executable for the user.
	 * 
	 * @param file
	 *            to make executable.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void makeExecutable(File file) throws FileNotFoundException, IOException
	{
		chmod("u+x", file.getAbsolutePath(), false, null);
	}

	/**
	 * Changes permissions on the path.
	 * 
	 * @param permissions
	 *            e.g. '755' or 'u+x'.
	 * @param path
	 *            file or directory.
	 * @param recursive
	 *            if true, changes all subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chmod(String permissions, File path, boolean recursive) throws FileNotFoundException, IOException
	{
		chmod(permissions, path.getAbsolutePath(), recursive, null);
	}

	/**
	 * Changes permissions on the paths represented by the list. List can contain
	 * either File objects or Strings.
	 * 
	 * @param permissions
	 *            e.g. '755' or 'u+x'.
	 * @param paths
	 *            files or directories.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chmod(String permissions, List paths, boolean recursive) throws FileNotFoundException, IOException
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator it = paths.iterator(); it.hasNext();) {
			Object path = it.next();
			if (path instanceof File)
				sb.append(((File) path).getAbsolutePath());
			else
				sb.append(path);
			sb.append(" ");
		}
		chmod(permissions, sb.toString().trim(), recursive, null);
	}

	/**
	 * Changes permissions on the paths represented by expression.
	 * 
	 * @param permissions
	 *            e.g. '755' or 'u+x'.
	 * @param pathExpression
	 *            unix-style path regular expression.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chmod(String permissions, String pathExpression, boolean recursive) throws FileNotFoundException, IOException
	{
		chmod(permissions, pathExpression, recursive, null);
	}

	/**
	 * Changes permissions on the paths represented by expression.
	 * 
	 * @param permissions
	 *            e.g. '755' or 'u+x'.
	 * @param pathExpression
	 *            unix-style path regular expression.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @param alternateShellPaths
	 *            to add to the default search list (/bin/sh, /usr/bin/sh).
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chmod(String permissions, String pathExpression, boolean recursive, List alternateShellPaths)
			throws FileNotFoundException, IOException
	{
		if (!isUNIX())
			throw new IOException("chmod only implemented on UNIX systems");
		File shell = findShell(alternateShellPaths);
		doChxxx(shell, "chmod", recursive, permissions, pathExpression);
	}

	/**
	 * Changes owner on the path.
	 * 
	 * @param owner
	 *            to change to.
	 * @param path
	 *            file or directory.
	 * @param recursive
	 *            if true, changes all subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chown(String owner, File path, boolean recursive) throws FileNotFoundException, IOException
	{
		chown(owner, path.getAbsolutePath(), recursive, null);
	}

	/**
	 * Changes owner on the paths represented by the list. List can contain
	 * either File objects or Strings.
	 * 
	 * @param owner
	 *            to change to.
	 * @param paths
	 *            file or directory.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chown(String owner, List paths, boolean recursive) throws FileNotFoundException, IOException
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator it = paths.iterator(); it.hasNext();) {
			Object path = it.next();
			if (path instanceof File)
				sb.append(((File) path).getAbsolutePath());
			else
				sb.append(path);
			sb.append(" ");
		}
		chown(owner, sb.toString().trim(), recursive, null);
	}

	/**
	 * Changes owner on the paths represented by expression.
	 * 
	 * @param owner
	 *            to change to.
	 * @param pathExpression
	 *            unix-style path regular expression.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @throws FileNotFoundException
	 *             if chmod executable not found
	 */
	public static void chown(String owner, String pathExpression, boolean recursive) throws FileNotFoundException, IOException
	{
		chown(owner, pathExpression, recursive, null);
	}

	/**
	 * Changes owner on the paths represented by expression.
	 * 
	 * @param owner
	 *            to change to.
	 * @param pathExpression
	 *            unix-style path regular expression.
	 * @param recursive
	 *            if true, changes all matching subdirectories and files.
	 * @param alternateShellPaths
	 *            to add to the default search list (/bin/sh, /usr/bin/sh).
	 * @throws FileNotFoundException
	 *             if chown executable not found
	 */
	public static void chown(String owner, String pathExpression, boolean recursive, List alternateShellPaths)
			throws FileNotFoundException, IOException
	{
		if (!isUNIX())
			throw new IOException("chown only implemented on UNIX systems");
		File shell = findShell(alternateShellPaths);
		doChxxx(shell, "chown", recursive, owner, pathExpression);
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * GREP //
	 * //////////////////////////////////////////////////////////////////////
	 */

	public static List grep(String grepCmd) throws Throwable
	{
		File exec = new File("/bin/sh");
		if (!exec.exists() || !exec.isFile()) {
			exec = new File("/usr/bin/sh");
			if (!exec.exists() || !exec.isFile())
				throw new FileNotFoundException("cannot find bash");
		}

		File tmp = new File("grep.sh");
		FileUtils.writeBytes(tmp, grepCmd.getBytes(), false);
		final List lines = new ArrayList();

		Runtime r = Runtime.getRuntime();

		Process p = null;
		Thread[] streams = new Thread[2];

		try {
			p = r.exec(new String[] { exec.getAbsolutePath(), tmp.getAbsolutePath() });

			streams[1] = streamConsumer(new InputStreamReader(p.getErrorStream()), true);
			streams[1].start();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			streams[0] = new Thread() {
				public void run()
				{

					while (true) {
						try {
							String line = reader.readLine();
							if (line == null)
								break;
							lines.add(line);
						} catch (EOFException eofe) {
							break;
						} catch (IOException ioe) {
							logger.error("grep output stream", ioe);
							return;
						}
					}

					try {
						reader.close();
					} catch (IOException ignored) {
					}
				}
			};
			streams[0].start();

			p.waitFor();
		} catch (InterruptedException ignored) {
		} finally {
			destroy(p, streams);
			tmp.delete();
		}
		return lines;
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * LINK //
	 * //////////////////////////////////////////////////////////////////////
	 */
	/**
	 * Calls ln -s using source and target files.
	 */
	public static void link(File source, File target) throws Throwable
	{
		File exec = new File("/bin/ln");
		if (!exec.exists() || !exec.isFile()) {
			exec = new File("/usr/bin/ln");
			if (!exec.exists() || !exec.isFile())
				throw new FileNotFoundException("cannot find executable 'ln'");
		}

		Process p = null;
		int exit = -1;
		try {
			p = Runtime.getRuntime()
					.exec(new String[] { exec.getAbsolutePath(), "-s", source.getAbsolutePath(), target.getAbsolutePath() });
			exit = p.waitFor();
		} catch (InterruptedException ignored) {
		} finally {
			destroy(p, null);
		}

		if (exit != 0)
			throw new RuntimeException("ln -s " + source + ", " + target + " failed with exit value " + exit);
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * TAR / UNTAR //
	 * ////////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Creates tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to tar file to be created.
	 * @param path
	 *            a file or directory to be tar'd up.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 */
	public static void tar(File tarPath, File path, boolean verbose, boolean zip) throws Throwable
	{
		File tarBaseDir = path.getParentFile();
		tar(tarPath, tarBaseDir, path.getName(), verbose, zip);
	}

	/**
	 * Creates tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to tar file to be created.
	 * @param tarBaseDir
	 *            absolute path to dir in which to execute the tar command.
	 * @param fileNames
	 *            list of names relative to base dir to be tar'd up.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 */
	public static void tar(File tarPath, File tarBaseDir, List fileNames, boolean verbose, boolean zip) throws Throwable
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator it = fileNames.iterator(); it.hasNext();) {
			Object name = it.next();
			if (name instanceof File)
				sb.append(((File) name).getName());
			else
				sb.append(name);
			sb.append(" ");
		}
		tar(tarPath, tarBaseDir, sb.toString(), verbose, zip);
	}

	/**
	 * Creates tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to tar file to be created.
	 * @param tarBaseDir
	 *            absolute path to dir in which to execute the tar command.
	 * @param sourceExpression
	 *            Unix-style path expression for files to be tar'd up.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 */
	public static void tar(File tarPath, File tarBaseDir, String sourceExpression, boolean verbose, boolean zip) throws Throwable
	{
		tar(tarPath, tarBaseDir, sourceExpression, verbose, zip, null);
	}

	/**
	 * Creates tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to tar file to be created.
	 * @param tarBaseDir
	 *            absolute path to dir in which to execute the tar command.
	 * @param sourceExpression
	 *            Unix-style path expression for files to be tar'd up.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 * @param alternateShellPaths
	 *            to the bash shell executable.
	 */
	public static void tar(File tarPath, File tarBaseDir, String sourceExpression, boolean verbose, boolean zip, List alternateShellPaths)
			throws FileNotFoundException, IOException
	{
		File shell = findShell(alternateShellPaths);
		StringBuffer options = new StringBuffer("cf");
		if (verbose)
			options.append("v");
		if (zip) {
			options.append("z");
		}
		doTar(shell, options.toString(), tarPath, sourceExpression, tarBaseDir);
	}

	/**
	 * Unpacks tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to file to be untar'd.
	 * @param destinationDir
	 *            absolute path to dir in which to execute the tar command.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 */
	public static void untar(File tarPath, File destinationDir, boolean verbose, boolean zip) throws FileNotFoundException, IOException
	{
		untar(tarPath, destinationDir, verbose, zip, null);
	}

	/**
	 * Unpacks tar archive.
	 * 
	 * @param tarPath
	 *            absolute path to file to be untar'd.
	 * @param destinationDir
	 *            absolute path to dir in which to execute the tar command.
	 * @param verbose
	 *            = -v
	 * @param zip
	 *            = -z
	 * @param alternateShellPaths
	 *            to the bash shell executable.
	 */
	public static void untar(File tarPath, File destinationDir, boolean verbose, boolean zip, List alternateShellPaths)
			throws FileNotFoundException, IOException
	{
		File shell = findShell(alternateShellPaths);
		StringBuffer options = new StringBuffer("xf");
		if (verbose)
			options.append("v");
		if (zip)
			options.append("z");
		doTar(shell, options.toString(), tarPath, null, destinationDir);
	}

	/*
	 * ////////////////////////////////////////////////////////////////////////
	 * PRINTING //
	 * ////////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Automatically recurs on Lists, Maps or Arrays. Calls XmlUtils.asXML is the
	 * object is not a string.
	 * <p>
	 * 
	 * @param o
	 *            to print.
	 * @param err
	 *            print to stderr.
	 */
	public static void print(Object o, boolean err)
	{
		if (o != null && !(o instanceof String)) {
			try {
				o = XmlUtils.asXML(o);
			} catch (Throwable t) {
				logger.error("could not pretty print " + o, t);
			}
		}
		if (err)
			System.err.println(o);
		else
			System.out.println(o);
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * DATE & TIME -- DEPRECATED, moved to DateUtils //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @deprecated Use DateUtils
	 */
	public static Date getDateFromTimeString(String timeString)
	{
		return DateUtils.getDateFromTimeString(timeString);
	} // getDateObject

	/**
	 * @deprecated Use DateUtils
	 */
	public static Date getDate(long millis)
	{
		return DateUtils.getDate(millis);
	} // getDateObject

	/**
	 * @deprecated Use DateUtils
	 */
	public static Long getTimeInMillis(String dateTime)
	{
		return DateUtils.getTimeInMillis(dateTime);
	} // getTimeInMillis

	/**
	 * @deprecated Use DateUtils
	 */
	public static Long getTimeInMillis(String dateTime, String format)
	{
		return DateUtils.getTimeInMillis(dateTime, format);
	} // getTimeInMillis

	/**
	 * @deprecated Use DateUtils
	 */
	public static Long getTimeInMillis(long millis)
	{
		return DateUtils.getTimeInMillis(millis);
	} // getTimeInMillis

	/**
	 * @deprecated Use DateUtils
	 */
	public static String getDateString(long millis)
	{
		return DateUtils.getDateString(millis);
	} // getDateString

	/**
	 * @deprecated Use DateUtils
	 */
	public static String getDateString(long millis, String format)
	{
		return DateUtils.getDateString(millis, format);
	} // getDateString

	/**
	 * @deprecated Use DateUtils
	 */
	public static String getDateString(long millis, DateFormat formatter)
	{
		return DateUtils.getDateString(millis, formatter);
	} // getDateString

	/**
	 * @deprecated Use DateUtils
	 */
	public static String getDateString(Date date)
	{
		return DateUtils.getDateString(date);
	} // getDateString

	/**
	 * @deprecated Use DateUtils
	 */
	public static String getDateString(Date date, String format)
	{
		return DateUtils.getDateString(date, format);
	} // dateTime

	/**
	 * @deprecated Use DateUtils
	 */
	public static Date getDateFromDateString(String dateTime)
	{
		return DateUtils.getDateFromDateString(dateTime);
	} // getDateFromDateString

	/**
	 * @deprecated Use DateUtils
	 */
	public static Date getDateFromDateString(String dateTime, String format)
	{
		return DateUtils.getDateFromDateString(dateTime, format);
	} // dateTime

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * TAIL -f //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Does tail -f on the given file, returning the process descriptor. Stderr
	 * is consumed immediately, but stdout should be handled by the caller.
	 * 
	 * @param path
	 *            file on which to call tail -f
	 * @return process descriptor
	 */
	public static Process tailF(File path, Thread[] streams) throws IOException
	{
		File exec = new File("/bin/tail");
		if (!exec.exists() || !exec.isFile()) {
			exec = new File("/usr/bin/tail");
			if (!exec.exists() || !exec.isFile())
				throw new FileNotFoundException("cannot find executable 'tail'");
		}

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(new String[] { exec.getAbsolutePath(), "-f", path.getAbsolutePath() });

		// consume it just in case
		Thread t = streamConsumer(new InputStreamReader(p.getErrorStream()), true);
		t.start();
		if (streams != null && streams.length > 0)
			streams[0] = t;

		// stdout should be handled by caller
		return p;
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * PROCESS & STREAMS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Handles the input and error stream of a process by writing them to the
	 * logger.
	 * 
	 * @param pd
	 *            process whose stream should be consumed.
	 */
	public static ExceptionPreservingThread[] logProcessStreams(final Process pd)
	{
		ExceptionPreservingThread[] t = new ExceptionPreservingThread[2];
		synchronized (pd) {
			t[0] = streamConsumer(new InputStreamReader(pd.getInputStream()), null, false);
			t[0].start();
			t[1] = streamConsumer(new InputStreamReader(pd.getErrorStream()), null, true);
			t[1].start();
		}
		return t;
	}

	/**
	 * Handles the input and error stream of a process by writing them to the
	 * logger.
	 * 
	 * @param pd
	 *            process whose stream should be consumed.
	 */
	public static ExceptionPreservingThread[] logProcessStreams(final Process pd, String filePrefix)
	{
		ExceptionPreservingThread[] t = new ExceptionPreservingThread[2];
		synchronized (pd) {
			t[0] = streamConsumer(new InputStreamReader(pd.getInputStream()), filePrefix, false);
			t[0].start();
			t[1] = streamConsumer(new InputStreamReader(pd.getErrorStream()), filePrefix, true);
			t[1].start();
		}
		return t;
	}

	/**
	 * Creates thread which consumes the stream by reading and logging.
	 * 
	 * @param reader
	 *            for the input stream
	 * @param err
	 *            if true, stream is an error stream.
	 */
	public static ExceptionPreservingThread streamConsumer(final InputStreamReader reader, final boolean err)
	{
		return streamConsumer(reader, null, err);
	}

	/**
	 * Creates thread which consumes the stream by reading and logging.
	 * 
	 * @param reader
	 *            for the input stream
	 * @param filePrefix
	 *            log to file whose path is this prefix, with extension ".out" or ".err"
	 * @param err
	 *            if true, stream is an error stream.
	 */
	public static ExceptionPreservingThread streamConsumer(final InputStreamReader reader, final String filePrefix, final boolean err)
	{
		return new ExceptionPreservingThread() {
			public void run()
			{
				FileWriter fw = null;
				try {

					if (filePrefix != null) {
						fw = new FileWriter(new File(filePrefix + (err ? ".err" : ".out")), false);
					}
				} catch (IOException ioe) {
					logger.error(ioe);
					fw = null;
				}

				char[] buffer = new char[NCSAConstants.STREAM_BUFFER_SIZE];
				int numBytes;

				List errors = null;

				while (true) {
					numBytes = 0;
					String error = null;
					try {
						numBytes = reader.read(buffer, 0, NCSAConstants.STREAM_BUFFER_SIZE);
					} catch (EOFException eofe) {
						break;
					} catch (IOException ioe) {
						logger.error(ioe);
						thrown = ioe;
						break;
					}

					if (numBytes == NCSAConstants.EOF)
						break;

					if (fw != null) {
						String s = new String(buffer, 0, numBytes);
						try {
							fw.write(s);
							fw.flush();
						} catch (IOException ioe) {
							logger.error(ioe);
							thrown = ioe;
							break;
						}
						if (err)
							error = s;
					} else if (!err) {
						logger.info(new String(buffer, 0, numBytes));
					} else {
						error = new String(buffer, 0, numBytes);
						logger.error(error);
					}

					if (error != null) {
						if (errors == null)
							errors = new ArrayList();
						errors.add(error);
					}
				}

				try {
					reader.close();
					if (fw != null) {
						fw.flush();
						fw.close();
					}
				} catch (IOException ignored) {
				}

				if (errors != null)
					thrown = new RuntimeException(errors.toString());
			}
		};
	} // streamConsumer

	/**
	 * Creates thread which consumes the stream by reading and appending to
	 * buffer
	 * 
	 * @param reader
	 *            for the input stream
	 * @param err
	 *            if true, stream is an error stream.
	 * @param output
	 *            of stream
	 */
	public static Thread streamConsumer(final InputStreamReader reader, final boolean err, final StringBuffer output)
	{
		return new Thread() {
			public void run()
			{
				char[] buffer = new char[NCSAConstants.STREAM_BUFFER_SIZE];
				int numBytes;

				while (true) {
					numBytes = 0;
					try {
						numBytes = reader.read(buffer, 0, NCSAConstants.STREAM_BUFFER_SIZE);
					} catch (EOFException eofe) {
						break;
					} catch (IOException ioe) {
						logger.error("streamConsumer", ioe);
						return;
					}
					if (numBytes == NCSAConstants.EOF)
						break;
					if (output != null)
						output.append(buffer, 0, numBytes);
				}

				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		};
	} // streamConsumer

	/**
	 * Calls destroy on the process in order to guarantee release of
	 * resources in underlying platform.
	 * 
	 * @param pd
	 *            process
	 * @param streamThreads
	 *            of the stream consumers
	 */
	public static void destroy(final Process pd, final Thread[] streamThread)
	{
		if (streamThread != null) {
			for (int i = 0; i < streamThread.length; i++) {
				join(streamThread[i]);
			}
		}

		if (pd != null)
			pd.destroy();
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * KILL //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Also kills child processes. If no root or tags are provided, will attempt
	 * to kill all the processes listed by ps -ef (ps -aj on Mac OS X). Uses -9
	 * (SIGKILL) if no signal is provided.
	 * 
	 * @param user
	 *            can be <code>null</code>
	 * @param rootPid
	 *            can be <code>null</code>
	 * @param tagToMatch
	 *            can be <code>null</code>
	 * @param signal
	 *            can be <code>null</code>
	 * @return the pids on which kill was called.
	 */
	public static List killAll(String user, String rootPid, String tagToMatch, String signal) throws Throwable
	{
		UnixProcessReader reader = new UnixProcessReader();
		reader.setUser(user);
		reader.setRoot(rootPid);
		reader.setTagToMatch(tagToMatch);
		reader.setSignal(signal);
		reader.killAll();
		return reader.getPids();
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * AUXILIARY METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Mini-StreamMonitor thread. Appends result to the "env" StringBuffer.
	 * 
	 * @param process
	 *            presumably corresponding to the exec'd 'printenv' command.
	 * @return the monitor thread.
	 */
	private static Thread getEnv(Process process, final StringBuffer envBuffer)
	{
		final BufferedReader esr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		new Thread() {
			public void run()
			{
				int bytes = 0;
				char[] buf = new char[NCSAConstants.STREAM_BUFFER_SIZE];
				while (!Thread.interrupted()) {
					try {
						bytes = esr.read(buf, 0, NCSAConstants.STREAM_BUFFER_SIZE);
						if (bytes == NCSAConstants.EOF)
							break;
					} catch (IOException ignored) {
						break;
					}
				}
				try {
					esr.close();
				} catch (IOException ignored) {
				}
			}
		}.start();

		final BufferedReader isr = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final Thread t = new Thread() {
			public void run()
			{
				int bytes = 0;
				char[] buf = new char[NCSAConstants.STREAM_BUFFER_SIZE];
				while (!Thread.interrupted()) {
					try {
						bytes = isr.read(buf, 0, NCSAConstants.STREAM_BUFFER_SIZE);
						if (bytes == NCSAConstants.EOF)
							break;
						if (bytes > 0)
							envBuffer.append(buf, 0, bytes);
					} catch (IOException ignored) {
						break;
					}
				}
				try {
					isr.close();
				} catch (IOException ignored) {
				}
			}
		};
		t.start();
		return t;
	} // getEnv

	/**
	 * Linear parser which assumes "name=value" syntax for env variable stream.
	 * Converts names from CAPS to miniscule and substitutes "." for any "_"
	 * characters. Also adds each environment variable definition to the 'env'
	 * List.
	 * 
	 * @param b
	 *            if true, sets System.property( name, value ) for each parsed
	 *            line.
	 * @param envBuffer
	 *            containing the "name=value" substrings.
	 * @return the envBuffer contents parsed into lines.
	 */
	private static List processEnv(StringBuffer envBuffer, boolean b)
	{
		List env = new ArrayList();
		StringReader sr = new StringReader(envBuffer.toString());
		StringBuffer NAME = new StringBuffer(0);
		StringBuffer name = new StringBuffer(0);
		StringBuffer value = new StringBuffer(0);
		int state = NCSAConstants.NAME;
		char[] next = new char[1];

		while (true) {
			try {
				if ((sr.read(next, 0, 1)) == NCSAConstants.EOF)
					break;
			} catch (IOException ignored) {
				break;
			}
			if (next[0] == NCSAConstants.LINE_SEP_CHAR) {
				if (value.indexOf(" ") > 0 || value.indexOf("*") > 0) {
					value.insert(0, "\"").append("\"");
				}
				env.add(NAME.toString() + "=" + value.toString());
				if (b)
					System.setProperty(name.toString(), value.toString());
				NAME.setLength(0);
				name.setLength(0);
				value.setLength(0);
				state = NCSAConstants.NAME;
			} else if (next[0] == '=') {
				state = NCSAConstants.VALUE;
			} else if (state == NCSAConstants.NAME) {
				if (next[0] == '_')
					name.append('.');
				else if (Character.isUpperCase(next[0]))
					name.append(Character.toLowerCase(next[0]));
				else
					name.append(next[0]);
				NAME.append(next[0]);
			} else {
				if (next[0] != 0)
					value.append(next[0]);
			}
		}
		return env;
	} // processEnv

	public static File findShell(List altPaths) throws FileNotFoundException
	{
		List possiblePaths = new ArrayList();
		possiblePaths.add("/bin/sh");
		possiblePaths.add("/usr/bin/sh");
		if (altPaths != null)
			possiblePaths.addAll(altPaths);
		String[] allPaths = (String[]) possiblePaths.toArray(new String[0]);
		for (int i = 0; i < allPaths.length; i++) {
			File exec = new File(allPaths[i]);
			if (exec.exists() && exec.isFile())
				return exec;
		}
		throw new FileNotFoundException("cannot locate shell: " + possiblePaths);
	}

	private static void doTar(File shell, String options, File tarPath, String tarArgument, File execDir) throws IOException
	{
		int exit = 0;
		StringBuffer sb = new StringBuffer();
		FileWriter fw = null;
		Process p = null;
		Thread[] streams = new Thread[2];
		try {
			File tmpShellScript = new File("tmpscript");
			fw = new FileWriter(tmpShellScript);
			if (execDir != null) {
				sb.append("cd ").append(execDir.getAbsolutePath()).append(NCSAConstants.LINE_SEP);
			}
			sb.append("tar ").append(options).append(" ").append(tarPath.getAbsolutePath());
			if (tarArgument != null)
				sb.append(" ").append(tarArgument);
			sb.append(NCSAConstants.LINE_SEP);
			logger.debug(shell.getAbsolutePath() + " " + sb.toString());
			fw.write(sb.toString());
			fw.flush();
			Runtime r = Runtime.getRuntime();
			p = r.exec(new String[] { shell.getAbsolutePath(), tmpShellScript.getAbsolutePath() });
			streams = logProcessStreams(p);
			exit = p.waitFor();
			tmpShellScript.delete();
		} catch (InterruptedException ignored) {
		} catch (IOException io) {
			throw new IOException("doTar: " + sb + " failed to execute:\n" + ExceptionUtils.getStackTrace(io));
		} finally {
			if (fw != null) {
				fw.close();
			}
			destroy(p, streams);
		}
		if (0 != exit)
			throw new IOException("doTar: " + sb + " exited with " + exit);
	}

	private static void doChxxx(File shell, String command, boolean recursive, String permissions, String pathExpression)
			throws IOException
	{
		int exit = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer error = new StringBuffer();
		FileWriter fw = null;
		Process p = null;
		Thread[] streams = new Thread[2];
		try {
			File tmpShellScript = new File("tmpscript");
			fw = new FileWriter(tmpShellScript);
			sb.append(command);
			sb.append(recursive ? " -R " : " ");
			sb.append(permissions);
			sb.append(" ");
			sb.append(pathExpression);
			sb.append(NCSAConstants.LINE_SEP);
			logger.debug(shell.getAbsolutePath() + " " + sb.toString());
			fw.write(sb.toString());
			fw.flush();
			Runtime r = Runtime.getRuntime();
			p = r.exec(new String[] { shell.getAbsolutePath(), tmpShellScript.getAbsolutePath() });
			streams[0] = streamConsumer(new InputStreamReader(p.getInputStream()), false, null);
			streams[1] = streamConsumer(new InputStreamReader(p.getErrorStream()), true, error);
			streams[0].start();
			streams[1].start();
			exit = p.waitFor();
			tmpShellScript.delete();
		} catch (InterruptedException ignored) {
		} catch (Throwable io) {
			throw new IOException(sb + " failed to execute:\n" + ExceptionUtils.getStackTrace(io));
		} finally {
			if (fw != null) {
				fw.close();
			}
			destroy(p, streams);
		}
		if (0 != exit) {
			if (error.length() > 0) {
				if (error.indexOf("No match") >= 0 || error.indexOf("No such") >= 0) {
					logger.error(sb + ": no matching files or directories");
					return;
				}
			}
			throw new IOException(sb + " exited with " + exit + ", " + error);
		}
	}
}

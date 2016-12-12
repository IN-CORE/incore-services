package ncsa.tools.common;

/**
 * Various constants used throughout OGRE.
 * 
 * @author Albert L. Rossi
 */
public class NCSAConstants
{
	/* General constants //////////////////////////////////////////////////// */

	/**
	 * All-purpose endpoint uri System property.
	 */
	public static final String ENDPOINT_URI = "endpoint.uri";

	/**
	 * All-purpose endpoint uri System property.
	 */
	public static final String GID = "gid";

	/**
	 * RMI port-range System property.
	 */
	public static final String RMI_SERVER_PORT_RANGE = "rmi.server.port.range";

	/**
	 * Load log4j properties from this RCP bundle.
	 */
	public static final String LOG4J_BUNDLE = "log4j.bundle";

	/**
	 * X509.
	 */
	public static final String PROXY_LOC_PROPERTY = "X509_USER_PROXY";

	/**
	 * Main service administrator's domain name.
	 */
	public static final String PRINCIPAL_ADMIN_DN = "principal.admin.dn";

	/**
	 * User name to use.
	 */
	public static final String USER = "USER_NAME";

	/**
	 * System dependent.
	 */
	public static final String LINE_SEP = System.getProperty("line.separator");

	/**
	 * System dependent.
	 */
	public static final char LINE_SEP_CHAR = System.getProperty("line.separator").charAt(0);

	/**
	 * System dependent.
	 */
	public static final String FILE_SEP = System.getProperty("file.separator");

	/**
	 * General marker for undefined values.
	 */
	public static final int UNDEFINED = -1;

	/**
	 * Java's end-of-file marker.
	 */
	public static final int EOF = -1; // DO NOT ALTER!

	/**
	 * Default value for bit-wise & and |.
	 */
	public static final int BIT_DEFAULT = 0; // DO NOT ALTER!

	/**
	 * Lowest precedence number for expression symbols.
	 */
	public static final int LOWEST_PRECEDENCE = 12; // DO NOT ALTER!

	/* Default Buffer sizes ///////////////////////////////////////////////// */

	/**
	 * Average size of internal IO buffers.
	 */
	public static final int STREAM_BUFFER_SIZE = 1024;

	/**
	 * Default size of internal IO buffers for file copy operation.
	 */
	public static final int COPY_BUFFER_SIZE = 64 * 1024;

	/**
	 * Default size of internal buffers for BufferedReader.
	 */
	public static final int READER_BUFFER_SIZE = 512 * 1024;

	/**
	 * Default increment size to use when sending local intermediate
	 * copy update events.
	 */
	public static final int UPDATE_CHUNK_SIZE = 1024 * 1024 * 10;

	/**
	 * Default protection buffer size for Grid FTP transfers.
	 */
	public static final int GRIDFTP_BUFFER_SIZE = 16384;

	/* Default Queue settings /////////////////////////////////////////////// */

	/**
	 * Size for default thread pools.
	 */
	public static final int DEFAULT_THREAD_POOL_SIZE = 10;

	/**
	 * Default size for throttling internal queues.
	 */
	public static final int DEFAULT_QUEUE_CAPACITY = 1000;

	/**
	 * Default capacity limit for refreshing queues from underlying store.
	 */
	public static final double DEFAULT_QUEUE_THRESHOLD = 0.67;

	/* Polling, sleeping, timeouts ////////////////////////////////////////// */

	/**
	 * Default initial latency during staging calls to mass-storage.
	 */
	public static final long STAGING_LATENCY = 60000;

	/**
	 * Timeout during staging calls to mass-storage, used after arrival
	 * of first file on disk.
	 */
	public static final long STAGING_SLEEP = 5000;

	/**
	 * Default timeout for polling of channel.
	 */
	public static final long NOTIFICATION_TIMEOUT = 3000;

	/**
	 * Default timeout for progress events.
	 */
	public static final long PROGRESS_WAIT = 500;

	/**
	 * General default timeout for polling.
	 */
	public static final long POLL_SLEEP = 1000;

	/**
	 * Default timeout for waking up the cleanup-daemon on stores.
	 */
	public static final long CLEANUP_TIMEOUT = 60000;

	/* Port numbers ///////////////////////////////////////////////////////// */

	/**
	 * Dedicated GridFTP port.
	 */
	public static final int DEFAULT_GRIDFTP_PORT = 2811;

	/**
	 * Dedicated SSH port.
	 */
	public static final int DEFAULT_SSH_PORT = 22;

	/* Notification and events ////////////////////////////////////////////// */

	/**
	 * Supplied event channel implementation.
	 */
	public static final String DEFAULT_FACTORY = "ncsa.notification.clients.xevents.factory.XEventsNotificationFactory";

	/**
	 * For the Xevents-style asynchronous publisher.
	 */
	public static final String DEFAULT_LOG_DIR = "etc/logs";

	/**
	 * Assumes presence of a local channel.
	 */
	public static final String DEFAULT_SINK = "http://127.0.0.1:8080/ogre-services/services/Channel";

	/**
	 * Assumes presence of a local channel.
	 */
	public static final String DEFAULT_SOURCE = "http://127.0.0.1:8080/ogre-services/services/Channel";

	/**
	 * "Indefinite wait" marker for Xevents-style asynchronous publisher.
	 */
	public static final String DEFAULT_WAIT = "0";

	/* Miscellaneous sizes and counts /////////////////////////////////////// */

	/**
	 * Default setting for pattern length when using the RTFilter.
	 */
	public static final int MAX_PATTERN_LENGTH = 128 * 1024;

	/**
	 * Default setting for tag length when using the RTFilter.
	 */
	public static final int MAX_TAG_LENGTH = 128;

	/**
	 * Default setting for max batch size (Xevents implementation).
	 */
	public static final int EVENT_BATCH_SIZE = 50;

	/* Delimiters /////////////////////////////////////////////////////////// */

	/**
	 * Separator for serial representation of Java Lists.
	 */
	public static final String DEFAULT_L_SEPARATOR = ", ";

	/**
	 * Closing symbol for serial representation of Java Lists.
	 */
	public static final String DEFAULT_L_END_DELIM = "]";

	/**
	 * Opening symbol for serial representation of Java Lists.
	 */
	public static final String DEFAULT_L_START_DELIM = "[";

	/**
	 * Symbol for key to value assignment in Java Maps.
	 */
	public static final String DEFAULT_ASSIGNMENT = "=";

	/**
	 * Closing symbol for serial representation of Java Maps.
	 */
	public static final String DEFAULT_M_END_DELIM = "}";

	/**
	 * Opening symbol for serial representation of Java Maps.
	 */
	public static final String DEFAULT_M_START_DELIM = "{";

	/* Date/Time //////////////////////////////////////////////////////////// */

	/**
	 * Default date-time string format.
	 */
	public static final String DEFAULT_FORMAT = "yyyy/MM/dd HH:mm:ss";

	/* Comparators and containment operators //////////////////////////////// */

	/**
	 * '==' comparator (reference or primitive equality).
	 */
	public static final int EQ = 10010;

	/**
	 * Java 'equals' comparator for all objects (except primitive wrappers).
	 */
	public static final int EQUALS = 10011;

	/**
	 * Greater-than-or-equal-to comparator.
	 */
	public static final int GE = 10012;

	/**
	 * Greater-than comparator.
	 */
	public static final int GT = 10013;

	/**
	 * Less-than-or-equal-to comparator.
	 */
	public static final int LE = 10014;

	/**
	 * Less-than comparator.
	 */
	public static final int LT = 10015;

	/**
	 * Negation of EQ.
	 */
	public static final int NE = 10016;

	/**
	 * Negation of EQUALS.
	 */
	public static final int NEQUALS = 10017;

	/**
	 * Compare file contents.
	 */
	public static final int CONTENTS = 10020;

	/**
	 * Compare file lengths.
	 */
	public static final int LENGTH = 10021;

	/**
	 * Comapre path names.
	 */
	public static final int PATH = 10022;

	/**
	 * Compare file timestamps.
	 */
	public static final int TIMESTAMP = 10023;

	/**
	 * Check if an object contains a given class.
	 */
	public static final int FCLASS = 10024;

	/**
	 * Check if an object contains a field by a given name.
	 */
	public static final int FNAME = 10025;

	/**
	 * Check if an object contains a field of a given type.
	 */
	public static final int FTYPE = 10026;

	/**
	 * Check if an object contains a fiels with a given value.
	 */
	public static final int FVALUE = 10027;

	/**
	 * Check if a string matches by regexp
	 */
	public static final int MATCHES = 10028;

	/**
	 * Check if a string doesn't match by regexp
	 */
	public static final int NMATCHES = 10029;

	/* Types /////////////////////////////////////////////////////////////// */

	/**
	 * RegExFilterQueue semantics: indicates that multiple and overlapping
	 * publications are enabled; each filter in the queue is applied
	 * on every intercepted character sequence.
	 */
	public static final int MANY_TO_ONE = 10100;

	/**
	 * RegExFilterQueue semantics: indicates that the filter will return at most
	 * one match per character sequence (no overlapping); publication
	 * is determined by the first filter matched on the sequence.
	 */
	public static final int ONE_TO_ONE = 10101;

	/**
	 * RegExFilterQueue semantics: same as ONE_TO_ONE; a priority queue based on
	 * the running count of matches is maintained
	 */
	public static final int PRIORITY = 10102;

	/**
	 * RTFilter state: still looking for end tag.
	 */
	public static final int LOOKING = 10110;

	/**
	 * RTFilter state: a match for the tag was found.
	 */
	public static final int MATCHED = 10111;

	/**
	 * RTFilter state: no (intial tag) match (yet).
	 */
	public static final int UNMATCHED = 10112;

	/**
	 * Type marker for file read.
	 */
	public static final int BOOL = 10130; // Read

	/**
	 * Type marker for file read.
	 */
	public static final int BYTE = 10131;

	/**
	 * Type marker for file read.
	 */
	public static final int CHAR = 10132;

	/**
	 * Type marker for file read - carriage return.
	 */
	public static final int CRET = 10133;

	/**
	 * Type marker for file read.
	 */
	public static final int DOUB = 10134;

	/**
	 * Type marker for file read.
	 */
	public static final int FLOT = 10135;

	/**
	 * Type marker for file read.
	 */
	public static final int INTG = 10136;

	/**
	 * Type marker for file read.
	 */
	public static final int LONG = 10137;

	/**
	 * Type marker for file read - new line.
	 */
	public static final int NEWL = 10138;

	/**
	 * Type marker for file read.
	 */
	public static final int SHRT = 10139;

	/**
	 * Type marker for file read - unsigned byte.
	 */
	public static final int UBYT = 10140;

	/**
	 * Type marker for file read - unsigned short.
	 */
	public static final int USHT = 10141;

	/**
	 * Type marker for file read - UTF encoded char.
	 */
	public static final int UTFC = 10142;

	/**
	 * Type marker for file read - white space.
	 */
	public static final int WSPC = 10143;

	/**
	 * Type marker for file read.
	 */
	public static final int TEXT = 10144;

	/**
	 * System property name.
	 */
	public static final int NAME = 10150;

	/**
	 * System property value.
	 */
	public static final int VALUE = 10151;

	/**
	 * EDateTime: express as java.util.Date.
	 */
	public static final int DATE_OBJECT = 10160;

	/**
	 * EDateTime: express as a long (milliseconds).
	 */
	public static final int TO_MILLIS = 10161;

	/**
	 * EDateTime: express as a date-time String.
	 */
	public static final int TO_STRING = 10162;

	/* Task and Handler options ///////////////////////////////////////////// */

	/**
	 * ECondBlock should behave as if ...; else if ...; ... else.
	 */
	public static final int ELSE_IF = 10200;

	/**
	 * ECondBlock should behave as a switch with no break statements.
	 */
	public static final int FALL_THROUGH = 10201;

	/**
	 * ECondBlock should behave as if ...; if ...; if ...; ... .
	 */
	public static final int ONE_ARMED = 10202;

	/**
	 * UriStageFrom flag for applying chmod to both files and directories.
	 */
	public static final int CHMOD_BOTH = 10220;

	/**
	 * UriStageFrom flag for applying chmod to directories.
	 */
	public static final int CHMOD_DIRS = 10221;

	/**
	 * UriStageFrom flag for applying chmod to files.
	 */
	public static final int CHMOD_FILES = 10222;

	/**
	 * UriStageFrom flag for not applying chmod.
	 */
	public static final int NO_CHMOD = 10223;

	/**
	 * UriStageFrom flag for not untar'ing any tar-balls.
	 */
	public static final int NO_UNTAR = 10224;

	/**
	 * UriStageFrom flag for untar'ing to directory based on tar-ball name.
	 */
	public static final int UNTAR_TO_FNAME = 10225;

	/**
	 * UriStageFrom flag for untar'ing to parent directory.
	 */
	public static final int UNTAR_TO_PARENT = 10226;

	/**
	 * GridFTP: no data-channel authentication.
	 */
	public static final int DCAU_NONE = 10230;

	/**
	 * GridFTP: data-channel authentication = self.
	 */
	public static final int DCAU_SELF = 10231;

	/**
	 * GridFTP: operation = 'get'.
	 */
	public static final int GET = 10232;

	/**
	 * GridFTP: operation = 'put'.
	 */
	public static final int PUT = 10233;

	/**
	 * GridFTP: operation = 'transfer'.
	 */
	public static final int TRANSFER = 10234;

	/**
	 * Write file using binary types.
	 */
	public static final int BINARY = 10240;

	/**
	 * Write file as characters (bytes).
	 */
	public static final int CHARS = 10241;

	/**
	 * Write file using UTF encoding.
	 */
	public static final int UTF = 10243;

	/**
	 * Uri handler operation.
	 */
	public static final int COPY = 10250;

	/**
	 * Uri handler operation.
	 */
	public static final int MOVE = 10251;

	/**
	 * Uri handler operation.
	 */
	public static final int CREATE = 10252;

	/**
	 * Uri handler operation.
	 */
	public static final int DELETE = 10253;

	/**
	 * Uri handler operation.
	 */
	public static final int MKDIRS = 10254;

	/**
	 * Uri handler operation.
	 */
	public static final int TOUCH = 10255;

	/**
	 * Uri handler operation.
	 */
	public static final int STAGE = 10256;

	/**
	 * Uri handler setting.
	 */
	public static final int IO_STREAMS = 10260;

	/**
	 * Uri handler setting.
	 */
	public static final int NIO_CHANNELS = 10261;

	/**
	 * Uri handler setting.
	 */
	public static final int UNIX_COPY = 10262;

	/* Expressions ////////////////////////////////////////////////////////// */

	/**
	 * Expression function: Math.abs.
	 */
	public static final int ABS = 10300;

	/**
	 * Expression function: Math.acos.
	 */
	public static final int ACOS = 10301;

	/**
	 * Expression operator: '&'.
	 */
	public static final int AMPS = 10302;

	/**
	 * Expression operator: &&.
	 */
	public static final int AND = 10303;

	/**
	 * Expression function: Math.asin.
	 */
	public static final int ASIN = 10304;

	/**
	 * Expression operator: '*'.
	 */
	public static final int ASTR = 10305;

	/**
	 * Expression function: Math.atan.
	 */
	public static final int ATAN = 10306;

	/**
	 * Expression function: Math.atan2.
	 */
	public static final int ATAN2 = 10307;

	/**
	 * Expression operator: '^'.
	 */
	public static final int CART = 10308;

	/**
	 * Expression function: Math.ceil.
	 */
	public static final int CEIL = 10309;

	/**
	 * Expression function: Math.cos.
	 */
	public static final int COS = 10310;

	/**
	 * Expression operator: '=='.
	 */
	public static final int EQUL = 10311;

	/**
	 * Expression operator: '!'.
	 */
	public static final int EXCL = 10312;

	/**
	 * Expression function: Math.exp.
	 */
	public static final int EXP = 10313;

	/**
	 * Expression function: Math.floor.
	 */
	public static final int FLOOR = 10314;

	/**
	 * Expression operator: '>'.
	 */
	public static final int GRTR = 10315;

	/**
	 * Expression operator: '>='.
	 */
	public static final int GTOE = 10316;

	/**
	 * Expression operator: '--'.
	 */
	public static final int HYHY = 10317;

	/**
	 * Expression operator: '-'.
	 */
	public static final int HYPH = 10318;

	/**
	 * Expression function: Math.IEEEremainder.
	 */
	public static final int IEEE = 10319;

/**
    *  Expression operator: '<<'.
    */
	public static final int LEFT = 10320;

/**
    *  Expression operator: '<'.
    */
	public static final int LESS = 10321;

	/**
	 * Expression function: Math.log.
	 */
	public static final int LOG = 10322;

	/**
	 * Expression operator: '>='.
	 */
	public static final int LTOE = 10324;

	/**
	 * Expression operator: Math.max.
	 */
	public static final int MAX = 10325;

	/**
	 * Expression operator: Math.min.
	 */
	public static final int MIN = 10326;

	/**
	 * Expression operator: '!='.
	 */
	public static final int NEQL = 10327;

	/**
	 * Expression operator: '||'.
	 */
	public static final int OR = 10328;

	/**
	 * Expression operator: '|'.
	 */
	public static final int PIPE = 10329;

	/**
	 * Expression operator: '++'.
	 */
	public static final int PLPL = 10330;

	/**
	 * Expression operator: '+'.
	 */
	public static final int PLUS = 10331;

	/**
	 * Expression operator: Math.pow.
	 */
	public static final int POW = 10332;

	/**
	 * Expression operator: '%'.
	 */
	public static final int PRCT = 10333;

	/**
	 * Expression operator: Math.random.
	 */
	public static final int RANDOM = 10334;

	/**
	 * Expression operator: '>>'.
	 */
	public static final int RGHT = 10335;

	/**
	 * Expression function: Math.rint.
	 */
	public static final int RINT = 10336;

	/**
	 * Expression function: Math.round.
	 */
	public static final int ROUND = 10337;

	/**
	 * Expression function: Math.sin.
	 */
	public static final int SIN = 10338;

	/**
	 * Expression operator: '/'.
	 */
	public static final int SLSH = 10339;

	/**
	 * Expression function: Math.sqrt.
	 */
	public static final int SQRT = 10340;

	/**
	 * Expression function: Math.tan.
	 */
	public static final int TAN = 10341;

	/**
	 * Expression operator: '~'.
	 */
	public static final int TLDE = 10342;

	/**
	 * Expression operator: '>>>'.
	 */
	public static final int ZRGT = 10343;

	/**
	 * Expression operator: 'equals'.
	 */
	public static final int SEQL = 10350;

	/**
	 * Expression operator: 'equals'.
	 */
	public static final int SEQN = 10351;

	/**
	 * Expression operator: 'startsWith'.
	 */
	public static final int STRT = 10352;

	/**
	 * Expression operator: 'endsWith'.
	 */
	public static final int ENDS = 10353;

	/**
	 * Expression operator: 'indexOf'.
	 */
	public static final int INDX = 10354;

	/**
	 * Expression operator: 'lastIndexOx'.
	 */
	public static final int LNDX = 10355;

	/**
	 * Expression operator: 'contains'.
	 */
	public static final int CTNS = 10356;

}

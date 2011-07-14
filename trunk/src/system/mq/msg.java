/*
 * This class contains the constant values that are used to exchange messages
 * at the msg Queue and Network components or other components in the future
 */

package system.mq;

/**
 *
 * @author Nuno Brito
 */
public interface msg {
    public static final String

    // popular tables
            TABLE_REFERENCE = "reference",
            TABLE_FILE = "REM_FILE",
            TABLE_FILE_NAME = "name",
            TABLE_CHECKSUM = "REM_CHECKSUM",
            TABLE_FILE_CONTENT = "REM_FILE_CONTENT",
            TABLE_FILE_SIGNATURE = "REM_FILE_SIGNATURE",
            TABLE_FILE_PATH = "path",
            TABLE_COMMENTS = "comments",
            TABLE_AUTHOR = "author",
            TABLE_FILE_WIN16 = "REM_FILE_WIN16",
            TABLE_FILE_WIN32 = "win32",
            TABLE_FILE_WIN64 = "REM_FILE_WIN64",
            TABLE_FILE_WIN128 = "REM_FILE_WIN128",

        TABLE_FILE_SHA1 = "sha1",
        TABLE_FILE_SHA256 = "sha256",
        TABLE_FILE_SHA512 = "sha512",
        TABLE_FILE_CRC32 = "crc32",
        TABLE_FILE_MD5 = "md5",


    // popular fields
            FIELD_ID = "id",
            FIELD_ID_SERIAL = "id_serial",
            FIELD_ID_NAME = "id_name",
            FIELD_FROM = "mqFROM", // Where does this msg comes from?
            FIELD_TO = "mqTO", // to whom is it destined? (triumvir, ...)
            FIELD_ADDRESS = "ADDRESS",
            FIELD_CREATED = "mqTIMESTAMP", // when was it received?
            FIELD_TICKET = "TICKET", // when was it received?
            FIELD_STATUS = "STATUS", // used for external connection pooling
            FIELD_PARAMETERS = "mqPARAMETERS", // the msg payload
            FIELD_MINITICKET = "MINITICKET",
            FIELD_TASK = "TASKNAME",
            FIELD_KEY = "pKEY",
            FIELD_VALUE = "pVALUE",
            FIELD_SCORE = "SCORE", // the score of a given client
            FIELD_NAME = "pNAME", // specify a name
            FIELD_PARENT = "pPARENT", // the parent of a given process
            FIELD_UPDATED = "pTIMEUPDATED", // when was it last updated?

            FIELD_REFERENCE = "cREFERENCE",
            FIELD_CONTENT = "cCONTENT",
            FIELD_ASH = "cASH",
            FIELD_HASH_MD5 = "cASH_MD5",
            FIELD_HASH_SHA1 = "cASH_SHA1",
            FIELD_ASH_SHA2 = "cASH_SHA2",
            FIELD_ASH_SHA256 = "cASH_SHA-256",
            FIELD_ASH_SHA512 = "cASH_SHA-512",
            FIELD_HASH_CRC32 = "cASH_CRC32",
            FIELD_VERSION = "cVERSION",
            FIELD_ARCH = "cARCHITECTURE",
            FIELD_LANGUAGE = "cLANGUAGE",
            FIELD_SIZE = "cSIZE",
            FIELD_DATE_CREATED = "cDATE_CREATED",
            FIELD_DATE_UPDATED = "cDATE_UPDATED",
            FIELD_WIN32 = "win32",
            FIELD_WIN32_VERSION = "win32_version",
            FIELD_WIN32_LANG = "win32_lang",
            FIELD_WIN32_ARCH = "win32_arch",


            FIELD_FILESIGNATURES = "cSIGNATURES",
            FIELD_FILENAME = "cFILENAME",
            FIELD_FILECONTENTS = "cFILECONTENTS",
            FIELD_COUNT = "COUNT",
            FIELD_AUTHOR = "cAUTHOR",
            FIELD_RANKING = "cRANKING",
            FIELD_FILEPATH = "cPATH",
            FIELD_COMMENTS = "cCOMMENTS",
            FIELD_FLAG = "cFLAG",
            FIELD_TIMESTAMP = "cTIMESTAMP",
            FIELD_TYPE_CHECKSUM = "cT_CHECK",
            FIELD_CHECKSUM = "cCHECKSUM",
            FIELD_ID_GENS = "cID_GENS",
            FIELD_PATH = "cPATH",

            FIELD_WHO = "cWHO",
            FIELD_MONTH = "cMonth",
            FIELD_DAY = "cDay",
            FIELD_HOUR = "cHour",

    // related to network
            FIELD_MESSAGE = "MESSAGE",
            FIELD_HOST = "HOST",
            FIELD_ORIGIN = "ORIGIN",
            FIELD_URL = "URL",
            FIELD_DIR = "DIR",
            FIELD_PORT = "PORT",
            FIELD_LISTEN = "LISTEN",
            FIELD_TIMEOUT = "TIMEOUT",
            FIELD_INTERVAL = "INTERVAL",
            FIELD_DEPTH = "DEPTH",

    // related to scores
            FIELD_CPU = "CPU",
            FIELD_RAM = "RAM",
            FIELD_DISK = "DISK",
            FIELD_BANDWIDTH = "BANDWIDTH",
            FIELD_UPTIME = "UPTIME",

    // parameters
            DELETE = "DELETE",
            LISTEN = "LISTEN",
            FORCE_FINISH = "FORCE_FINISH",
            NO_STATS = "NO_STATS",
            DIR = "DIR",
            SCAN = "SCAN",
            PROCESS = "process",
            THROTTLE = "THROTTLE",
            CHANGE_STATUS = "change_status",
            APPS = "apps",

   // container related
            RANK = "rank",
            COUNT = "count",
            CHECKSUM = "checksum",

            SPLIT = ",\\s", // the character used to split strings
            //SPLIT = "##", // the character used to split strings
    // typical roles
            
            sentinel = "sentinel",
            sentinel_gui = sentinel + "/GUI",
            sentinel_hot_folders = sentinel + "/hot_folders",
            sentinel_scanner = sentinel + "/scanner",
            sentinel_indexer = sentinel + "/indexer",
            sentinel_usb = sentinel + "/usb",
            sentinel_snapshot = sentinel + "/snapshot",

            centrum = "centrum",
            centrum_server = "centrum/server",
            centrum_client = "centrum/client",
            centrum_address = "centrum/address",

            triumvir = sentinel + "/triumvir",
            triumvir_server = "triumvir/server",
            triumvir_client = "triumvir/client",
            triumvir_address = "centrum/address",


            //manager = "system/manager", // we wanted it liked this in the future..
            manager = "manager",
            trayicon = "system/trayicon",

    // typical values for test cases
            PORT_A = "3000",
            PORT_B = "3001",
            PORT_C = "3002",
            addressA = "localhost:"+PORT_A,
            addressB = "localhost:"+PORT_B,
            addressC = "localhost:"+PORT_C,

    // interesting
            TRUE = "true",
            FALSE = "false",
            LOCK = "LOCK",

    // actions
            ACTION = "action"
            ;

    // related to processes
        public final static int
            MAINTENANCE = 4,
            LISTENING = 3,
            RUNNING = 2,
            ACTIVE = 2,
            STARTING = 1,
            SUSPENDED = 1,
            PAUSED = 1,
            INACTIVE = 0,
            STOPPED = 0,
            ERROR = -1,
     // related to log entry types
            INFO = 30,
            DEBUG = 31,
            EXTRA = 32,
            ROUTINE = 33,
            WARNING = 34,
            //ERROR = 30, defined previously, no problem in using the same
            
     // related to messages
            COMPLETED = 20,
            ACCEPTED = 10,
            FORGET = 6,
            PENDING = 5,
            EXPIRED = 4,
            IGNORED = 3,
            TIMEOUT = -10,
            REFUSED = -2,
            CONFLICT = -3,

       // related to status commands
            START = 100,
            //STOP = 101,
            //PAUSE = 102,
            RESUME = 103,
            CLOSE = 104;
            

}

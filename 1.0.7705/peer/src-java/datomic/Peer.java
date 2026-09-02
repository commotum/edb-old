package datomic;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.Connection;
import datomic.Database;
import datomic.QueryRequest;
import datomic.Util;
import datomic.functions.Fn;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Main entry point for database administration, connections, queries,
 * identifiers, and database functions.
 *
 * <p>The peer library runs queries and database access in the application
 * process and communicates with transactor and storage services as needed.</p>
 */
public class Peer {
    private static final Var REQUIRE = RT.var((String)"clojure.core", (String)"require");
    private static final Var CANCEL;
    private static final Var SQUUID;
    private static final Var SQUUID_TIME;
    private static final Var ID_LITERAL;
    private static final Var FUNCTION;
    private static final Var Q;
    private static final Var QUERY;
    private static final Var QSEQ;
    private static final Var CREATE_DATABASE;
    private static final Var RENAME_DATABASE;
    private static final Var DELETE_DATABASE;
    private static final Var LIST_BACKUPS;
    private static final Var GET_DATABASE_NAME;
    private static final Var CONNECT_URI;
    private static final Var TO_T;
    private static final Var TO_TX;
    private static final Var PART;
    private static final Var RESOLVE_TEMPID;
    private static final Var SHUTDOWN;
    private static final Var ADMINISTER_SYSTEM;

    private Peer() {
    }

    /**
     * Connects to a database described by a URI or supported parameter map.
     *
     * <p>Connections are thread-safe, long-lived, and cached by URI. Repeated
     * calls with the same URI return the same connection. Read-only connections
     * are fixed at the value observed when connected and are not cached.</p>
     *
     * @param uriOrMap database URI or protocol-specific parameter map
     * @return a connection to the database
     */
    public static Connection connect(Object uriOrMap) {
        return (Connection)CONNECT_URI.invoke(uriOrMap);
    }

    /**
     * Creates a database. This operation is idempotent.
     *
     * @param uriOrMap database URI or protocol-specific parameter map
     * @return {@code true} when the database was created; {@code false} when it
     *         already existed
     */
    public static boolean createDatabase(Object uriOrMap) {
        return (Boolean)CREATE_DATABASE.invoke(uriOrMap);
    }

    /**
     * Renames a database.
     *
     * @param uriOrMap database URI or protocol-specific parameter map
     * @param newName new database name, without a URI prefix
     * @return {@code true} when the rename succeeded
     */
    public static boolean renameDatabase(Object uriOrMap, String newName) {
        return (Boolean)RENAME_DATABASE.invoke(uriOrMap, (Object)newName);
    }

    /**
     * Deletes a database.
     *
     * @param uriOrMap database URI or protocol-specific parameter map
     * @return {@code true} when deletion occurred
     */
    public static boolean deleteDatabase(Object uriOrMap) {
        return (Boolean)DELETE_DATABASE.invoke(uriOrMap);
    }

    /**
     * Returns database names available at a system URI.
     *
     * <p>In URI form, replace the database-name component with {@code *}. In
     * map form, omit {@code :db-name}.</p>
     *
     * @param uriOrMap system URI or parameter map
     * @return available database names
     */
    public static List<String> getDatabaseNames(Object uriOrMap) {
        return (List)GET_DATABASE_NAME.invoke(uriOrMap);
    }

    /**
     * Lists available backup points in descending t order.
     *
     * <p>Each backup includes its basis t and a URI accepted by
     * {@link #connect(Object)}.</p>
     *
     * @param backupUri backup storage URI
     * @return backup information
     */
    public static Map<Object, Object> listBackups(String backupUri) {
        return (Map)LIST_BACKUPS.invoke(backupUri);
    }

    /**
     * Performs a system administration operation.
     *
     * @param options operation and parameters
     * @return data describing the result
     * @throws RuntimeException when the operation fails
     */
    public static Object administerSystem(Map options) {
        return ADMINISTER_SYSTEM.invoke((Object)options);
    }

    /**
     * Constructs a semi-sequential UUID whose most significant 32 bits encode
     * the current time rounded to seconds.
     *
     * <p>Semi-sequential UUIDs reduce fragmentation when used in indexes.</p>
     *
     * @return a new semi-sequential UUID
     */
    public static UUID squuid() {
        return (UUID)SQUUID.invoke();
    }

    /**
     * Extracts the millisecond time component of a semi-sequential UUID.
     *
     * @param squuid UUID produced by {@link #squuid()}
     * @return time in the form used by {@link System#currentTimeMillis()}
     */
    public static long squuidTimeMillis(UUID squuid) {
        return (Long)SQUUID_TIME.invoke((Object)squuid);
    }

    /**
     * Generates a temporary entity id in a partition.
     *
     * @param partition keyword identifying the partition
     * @return a temporary entity id
     */
    public static Object tempid(Object partition) {
        return ID_LITERAL.invoke((Object)new Object[]{partition});
    }

    /**
     * Generates a temporary entity id with a caller-supplied id number.
     *
     * @param partition keyword identifying the partition
     * @param idNumber value in the range {@code (-1000000, -1]}
     * @return a temporary entity id containing {@code idNumber}
     */
    public static Object tempid(Object partition, long idNumber) {
        return ID_LITERAL.invoke((Object)new Object[]{partition, idNumber});
    }

    /**
     * Returns the t associated with a transaction id.
     *
     * @param tx transaction id
     * @return the corresponding t
     */
    public static long toT(Object tx) {
        return (Long)TO_T.invoke(tx);
    }

    /**
     * Returns the transaction id associated with a t value.
     *
     * @param t database t
     * @return the corresponding transaction id
     */
    public static Object toTx(long t) {
        return TO_TX.invoke((Object)t);
    }

    /**
     * Returns the partition encoded in an entity id.
     *
     * @param entityId entity id
     * @return the partition id
     */
    public static Object part(Object entityId) {
        return PART.invoke(entityId);
    }

    /**
     * Executes a Datalog query and returns relation results.
     *
     * <p>This signature supports relation find specifications. Scalar,
     * collection, and tuple find specifications are available through
     * {@link #query(Object, Object...)}.</p>
     *
     * @param query query map, list form, or EDN string
     * @param inputs values bound by the query's {@code :in} clause
     * @return relation results as a collection of tuples
     */
    public static Collection<List<Object>> q(Object query, Object ... inputs) {
        return (Collection)Q.invoke(query, (Object)inputs);
    }

    /**
     * Executes a Datalog query locally in the peer process.
     *
     * <p>Query supports joins, rules, predicates, functions, aggregates, pull,
     * and each find shape. Intermediate and final result sets must fit in
     * memory.</p>
     *
     * @param <T> result type selected by the find specification
     * @param query query map, list form, or EDN string
     * @param inputs values bound by the query's {@code :in} clause
     * @return data shaped by the find specification
     */
    public static <T> T query(Object query, Object ... inputs) {
        return (T)Q.invoke(query, (Object)inputs);
    }

    /**
     * Executes a query described by a request object.
     *
     * @param <T> result type selected by the find specification
     * @param queryRequest query, inputs, and optional timeout
     * @return data shaped by the find specification
     */
    public static <T> T query(QueryRequest queryRequest) {
        return (T)QUERY.invoke((Object)queryRequest.asData());
    }

    /**
     * Executes a query whose item transformations are deferred until the
     * returned stream is consumed.
     *
     * <p>Lazy pull and transformation reduce peak memory use and time to the
     * first result.</p>
     *
     * @param query query map, list form, or EDN string
     * @param inputs values bound by the query's {@code :in} clause
     * @return a stream of query result items
     */
    public static Stream<Object> qseq(Object query, Object ... inputs) {
        return Util.streamOn((Iterable)QSEQ.invoke(query, (Object)inputs));
    }

    /**
     * Constructs a database function.
     *
     * <p>The definition map contains {@code :lang}, {@code :params}, and
     * {@code :code}. Java code is a method body for Object parameters and may
     * begin with imports. The result implements {@link Fn} and the
     * arity-specific {@code Fn0} through {@code Fn10} interface. It compiles
     * itself on first invocation and caches the compiled form.</p>
     *
     * @param definition function language, parameters, and code
     * @return the database function
     */
    public static Fn function(Map definition) {
        return (Fn)FUNCTION.invoke((Object)definition);
    }

    /**
     * Resolves a temporary id using a transaction report.
     *
     * @param db database returned under {@link Connection#DB_AFTER}
     * @param tempids value returned under {@link Connection#TEMPIDS}
     * @param tempid temporary id to resolve
     * @return assigned entity id
     */
    public static Object resolveTempid(Database db, Object tempids, Object tempid) {
        return RESOLVE_TEMPID.invoke((Object)db, tempids, tempid);
    }

    /**
     * Shuts down all peer resources and releases every connection.
     *
     * @param shutdownClojure {@code true} to release Clojure runtime resources;
     *                        Clojure applications that manage those resources
     *                        may pass {@code false}
     */
    public static void shutdown(boolean shutdownClojure) {
        SHUTDOWN.invoke((Object)shutdownClojure);
    }

    /**
     * Cancels the current query or transaction and reports an anomaly to its
     * caller.
     *
     * <p>The anomaly map requires {@code :cognitect.anomalies/category}, whose
     * value is {@code :cognitect.anomalies/incorrect} or
     * {@code :cognitect.anomalies/conflict}. An optional
     * {@code :cognitect.anomalies/message} becomes the exception message.
     * Additional keys must be namespace-qualified, and all values must be
     * Fressian-serializable.</p>
     *
     * @param anomaly anomaly data returned to the caller
     * @throws RuntimeException to abort the current operation
     */
    public static void cancel(Object anomaly) {
        CANCEL.invoke(anomaly);
    }

    static {
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.query"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.peer"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.function"));
        CANCEL = RT.var((String)"datomic.api", (String)"cancel");
        SQUUID = RT.var((String)"datomic.common", (String)"squuid");
        SQUUID_TIME = RT.var((String)"datomic.common", (String)"squuid-time-ms");
        ID_LITERAL = RT.var((String)"datomic.db", (String)"id-literal");
        FUNCTION = RT.var((String)"datomic.function", (String)"construct");
        Q = RT.var((String)"datomic.query", (String)"q");
        QUERY = RT.var((String)"datomic.query", (String)"query");
        QSEQ = RT.var((String)"datomic.query", (String)"qseq");
        CREATE_DATABASE = RT.var((String)"datomic.peer", (String)"create-database");
        RENAME_DATABASE = RT.var((String)"datomic.peer", (String)"rename-database");
        DELETE_DATABASE = RT.var((String)"datomic.peer", (String)"delete-database");
        LIST_BACKUPS = RT.var((String)"datomic.peer", (String)"list-backups");
        GET_DATABASE_NAME = RT.var((String)"datomic.peer", (String)"get-database-names");
        CONNECT_URI = RT.var((String)"datomic.peer", (String)"connect-uri");
        TO_T = RT.var((String)"datomic.db", (String)"eid->eidx");
        TO_TX = RT.var((String)"datomic.peer", (String)"t->tx");
        PART = RT.var((String)"datomic.db", (String)"partition-eid");
        RESOLVE_TEMPID = RT.var((String)"datomic.peer", (String)"resolve-tempid");
        SHUTDOWN = RT.var((String)"datomic.peer", (String)"shutdown");
        ADMINISTER_SYSTEM = RT.var((String)"datomic.peer", (String)"administer-system");
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
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

    public static Connection connect(Object uriOrMap) {
        return (Connection)CONNECT_URI.invoke(uriOrMap);
    }

    public static boolean createDatabase(Object uriOrMap) {
        return (Boolean)CREATE_DATABASE.invoke(uriOrMap);
    }

    public static boolean renameDatabase(Object uriOrMap, String newName) {
        return (Boolean)RENAME_DATABASE.invoke(uriOrMap, (Object)newName);
    }

    public static boolean deleteDatabase(Object uriOrMap) {
        return (Boolean)DELETE_DATABASE.invoke(uriOrMap);
    }

    public static List<String> getDatabaseNames(Object uriOrMap) {
        return (List)GET_DATABASE_NAME.invoke(uriOrMap);
    }

    public static Map<Object, Object> listBackups(String backupUri) {
        return (Map)LIST_BACKUPS.invoke(backupUri);
    }

    public static Object administerSystem(Map options) {
        return ADMINISTER_SYSTEM.invoke((Object)options);
    }

    public static UUID squuid() {
        return (UUID)SQUUID.invoke();
    }

    public static long squuidTimeMillis(UUID squuid2) {
        return (Long)SQUUID_TIME.invoke((Object)squuid2);
    }

    public static Object tempid(Object partition) {
        return ID_LITERAL.invoke((Object)new Object[]{partition});
    }

    public static Object tempid(Object partition, long idNumber) {
        return ID_LITERAL.invoke((Object)new Object[]{partition, idNumber});
    }

    public static long toT(Object tx) {
        return (Long)TO_T.invoke(tx);
    }

    public static Object toTx(long t) {
        return TO_TX.invoke((Object)t);
    }

    public static Object part(Object entityId) {
        return PART.invoke(entityId);
    }

    public static Collection<List<Object>> q(Object query2, Object ... inputs) {
        return (Collection)Q.invoke(query2, (Object)inputs);
    }

    public static <T> T query(Object query2, Object ... inputs) {
        return (T)Q.invoke(query2, (Object)inputs);
    }

    public static <T> T query(QueryRequest queryRequest) {
        return (T)QUERY.invoke((Object)queryRequest.asData());
    }

    public static Stream<Object> qseq(Object query2, Object ... inputs) {
        return Util.streamOn((Iterable)QSEQ.invoke(query2, (Object)inputs));
    }

    public static Fn function(Map m) {
        return (Fn)FUNCTION.invoke((Object)m);
    }

    public static Object resolveTempid(Database db2, Object tempids, Object tempid2) {
        return RESOLVE_TEMPID.invoke((Object)db2, tempids, tempid2);
    }

    public static void shutdown(boolean shutdownClojure) {
        SHUTDOWN.invoke((Object)shutdownClojure);
    }

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

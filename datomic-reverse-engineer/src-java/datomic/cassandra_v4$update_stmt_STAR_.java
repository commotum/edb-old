/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datastax.oss.driver.api.core.ConsistencyLevel
 *  com.datastax.oss.driver.api.core.DefaultConsistencyLevel
 *  com.datastax.oss.driver.api.core.cql.SimpleStatement
 *  com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder
 *  com.datastax.oss.driver.api.core.cql.Statement
 *  com.datastax.oss.driver.api.core.cql.StatementBuilder
 *  com.datastax.oss.driver.api.core.cql.SyncCqlSession
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.StatementBuilder;
import com.datastax.oss.driver.api.core.cql.SyncCqlSession;
import datomic.cassandra_v4$update_stmt_STAR_$fn__10105;

public final class cassandra_v4$update_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object session, Object table, Object id_key, Object col_names) {
        Object update_cql;
        Object object = table;
        table = null;
        Object object2 = col_names;
        col_names = null;
        Object object3 = id_key;
        id_key = null;
        Object object4 = update_cql = ((IFn)const__0.getRawRoot()).invoke((Object)"update ", object, (Object)" set ", ((IFn)const__1.getRawRoot()).invoke((Object)", ", ((IFn)const__2.getRawRoot()).invoke((Object)new cassandra_v4$update_stmt_STAR_$fn__10105(), object2)), (Object)" where ", ((IFn)const__3.getRawRoot()).invoke(object3), (Object)" = ? if rev = ?");
        update_cql = null;
        Statement ss = ((StatementBuilder)new SimpleStatementBuilder((String)object4)).setConsistencyLevel((ConsistencyLevel)DefaultConsistencyLevel.LOCAL_QUORUM).setIdempotence(Boolean.FALSE).build();
        Object object5 = session;
        session = null;
        Statement statement = ss;
        ss = null;
        return ((SyncCqlSession)object5).prepare((SimpleStatement)statement);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return cassandra_v4$update_stmt_STAR_.invokeStatic(object5, object6, object7, object8);
    }
}


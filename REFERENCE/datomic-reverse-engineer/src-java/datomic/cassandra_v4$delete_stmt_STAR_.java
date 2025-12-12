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

public final class cassandra_v4$delete_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object session, Object table, Object id_key) {
        Object object = table;
        table = null;
        Object object2 = id_key;
        id_key = null;
        Statement ss = ((StatementBuilder)new SimpleStatementBuilder((String)((IFn)const__0.getRawRoot()).invoke((Object)"delete from ", object, (Object)" where ", ((IFn)const__1.getRawRoot()).invoke(object2), (Object)" = ?"))).setIdempotence(Boolean.FALSE).setConsistencyLevel((ConsistencyLevel)DefaultConsistencyLevel.LOCAL_QUORUM).build();
        Object object3 = session;
        session = null;
        Statement statement = ss;
        ss = null;
        return ((SyncCqlSession)object3).prepare((SimpleStatement)statement);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cassandra_v4$delete_stmt_STAR_.invokeStatic(object4, object5, object6);
    }
}


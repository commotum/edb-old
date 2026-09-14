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

public final class cassandra_v4$insert_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"repeat");

    public static Object invokeStatic(Object session, Object table, Object col_names, Object consistent_QMARK_) {
        Object insert_cql;
        Object object = table;
        table = null;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke((Object)", ", col_names);
        Object object3 = col_names;
        col_names = null;
        Object object4 = consistent_QMARK_;
        consistent_QMARK_ = null;
        Object object5 = insert_cql = ((IFn)const__0.getRawRoot()).invoke((Object)"insert into ", object, (Object)" (", object2, (Object)") values (", ((IFn)const__1.getRawRoot()).invoke((Object)", ", ((IFn)const__2.getRawRoot()).invoke((Object)RT.count((Object)object3), ((IFn)const__4.getRawRoot()).invoke((Object)"?"))), (Object)")", (Object)(object4 != null && object4 != Boolean.FALSE ? " if not exists" : null));
        insert_cql = null;
        Statement ss = ((StatementBuilder)new SimpleStatementBuilder((String)object5)).setConsistencyLevel((ConsistencyLevel)DefaultConsistencyLevel.LOCAL_QUORUM).setIdempotence(Boolean.FALSE).build();
        Object object6 = session;
        session = null;
        Statement statement = ss;
        ss = null;
        return ((SyncCqlSession)object6).prepare((SimpleStatement)statement);
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
        return cassandra_v4$insert_stmt_STAR_.invokeStatic(object5, object6, object7, object8);
    }
}


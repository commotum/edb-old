/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datastax.driver.core.ConsistencyLevel
 *  com.datastax.driver.core.PreparedStatement
 *  com.datastax.driver.core.Session
 *  com.datastax.driver.core.policies.RetryPolicy
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RetryPolicy;
import datomic.cassandra$update_stmt_STAR_$fn__17729;

public final class cassandra$update_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__4 = RT.var((String)"datomic.cassandra", (String)"retry-policy");

    public static Object invokeStatic(Object session, Object table, Object id_key, Object col_names) {
        Object object = table;
        table = null;
        Object object2 = col_names;
        col_names = null;
        Object object3 = id_key;
        id_key = null;
        Object update_cql = ((IFn)const__0.getRawRoot()).invoke((Object)"update ", object, (Object)" set ", ((IFn)const__1.getRawRoot()).invoke((Object)", ", ((IFn)const__2.getRawRoot()).invoke((Object)new cassandra$update_stmt_STAR_$fn__17729(), object2)), (Object)" where ", ((IFn)const__3.getRawRoot()).invoke(object3), (Object)" = ? if rev = ?");
        Object object4 = session;
        session = null;
        Object object5 = update_cql;
        update_cql = null;
        PreparedStatement stmt = ((Session)object4).prepare((String)object5);
        stmt.setConsistencyLevel(ConsistencyLevel.QUORUM);
        stmt.setRetryPolicy((RetryPolicy)((IFn)const__4.getRawRoot()).invoke());
        PreparedStatement preparedStatement = stmt;
        stmt = null;
        return preparedStatement;
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
        return cassandra$update_stmt_STAR_.invokeStatic(object5, object6, object7, object8);
    }
}


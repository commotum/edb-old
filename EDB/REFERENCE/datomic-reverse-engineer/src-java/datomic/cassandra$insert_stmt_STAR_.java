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

public final class cassandra$insert_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__5 = RT.var((String)"datomic.cassandra", (String)"retry-policy");

    public static Object invokeStatic(Object session, Object table, Object col_names, Object consistent_QMARK_) {
        Object object = table;
        table = null;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke((Object)", ", col_names);
        Object object3 = col_names;
        col_names = null;
        Object object4 = consistent_QMARK_;
        consistent_QMARK_ = null;
        Object insert_cql = ((IFn)const__0.getRawRoot()).invoke((Object)"insert into ", object, (Object)" (", object2, (Object)") values (", ((IFn)const__1.getRawRoot()).invoke((Object)", ", ((IFn)const__2.getRawRoot()).invoke((Object)RT.count((Object)object3), ((IFn)const__4.getRawRoot()).invoke((Object)"?"))), (Object)")", (Object)(object4 != null && object4 != Boolean.FALSE ? " if not exists" : null));
        Object object5 = session;
        session = null;
        Object object6 = insert_cql;
        insert_cql = null;
        PreparedStatement stmt = ((Session)object5).prepare((String)object6);
        stmt.setConsistencyLevel(ConsistencyLevel.QUORUM);
        stmt.setRetryPolicy((RetryPolicy)((IFn)const__5.getRawRoot()).invoke());
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
        return cassandra$insert_stmt_STAR_.invokeStatic(object5, object6, object7, object8);
    }
}


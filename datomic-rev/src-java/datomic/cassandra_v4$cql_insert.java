/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.datastax.oss.driver.api.core.ConsistencyLevel
 *  com.datastax.oss.driver.api.core.PagingIterable
 *  com.datastax.oss.driver.api.core.cql.BoundStatement
 *  com.datastax.oss.driver.api.core.cql.PreparedStatement
 *  com.datastax.oss.driver.api.core.cql.ResultSet
 *  com.datastax.oss.driver.api.core.cql.Statement
 *  com.datastax.oss.driver.api.core.cql.SyncCqlSession
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.SyncCqlSession;

public final class cassandra_v4$cql_insert
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__7 = RT.var((String)"datomic.cassandra-v4", (String)"insert-stmt");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__9 = RT.classForName((String)"java.lang.Object");
    public static final Var const__10 = RT.var((String)"datomic.cassandra-v4", (String)"updated?");

    public static Object invokeStatic(Object session, Object table, Object ks, Object v_map, Object consistent_QMARK_) {
        Object object;
        BoundStatement boundStatement;
        Object stmt;
        Object object2 = v_map;
        v_map = null;
        Object object3 = ks;
        ks = null;
        Object col_vals = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(object2, object3));
        Object col_names = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), const__6.getRawRoot()), col_vals);
        Object object4 = table;
        table = null;
        Object object5 = col_names;
        col_names = null;
        Object object6 = stmt = ((IFn)const__7.getRawRoot()).invoke(session, object4, object5, consistent_QMARK_);
        stmt = null;
        Object object7 = col_vals;
        col_vals = null;
        BoundStatement G__10116 = ((PreparedStatement)object6).bind((Object[])((IFn)const__8.getRawRoot()).invoke(const__9, ((IFn)const__3.getRawRoot()).invoke(const__1.getRawRoot(), object7)));
        Object object8 = consistent_QMARK_;
        if (object8 != null && object8 != Boolean.FALSE) {
            BoundStatement boundStatement2 = G__10116;
            G__10116 = null;
            boundStatement = ((Statement)boundStatement2).setSerialConsistencyLevel(ConsistencyLevel.SERIAL);
        } else {
            boundStatement = G__10116;
            G__10116 = null;
        }
        BoundStatement bound = boundStatement;
        Object object9 = session;
        session = null;
        BoundStatement boundStatement3 = bound;
        bound = null;
        ResultSet res = ((SyncCqlSession)object9).execute((Statement)boundStatement3);
        Object object10 = consistent_QMARK_;
        consistent_QMARK_ = null;
        if (object10 != null && object10 != Boolean.FALSE) {
            ResultSet resultSet = res;
            res = null;
            object = ((IFn)const__10.getRawRoot()).invoke((Object)resultSet);
        } else {
            ResultSet resultSet = res;
            res = null;
            object = Util.identical((Object)((PagingIterable)resultSet).one(), null) ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return cassandra_v4$cql_insert.invokeStatic(object6, object7, object8, object9, object10);
    }
}


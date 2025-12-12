/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.datastax.oss.driver.api.core.ConsistencyLevel
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
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.SyncCqlSession;

public final class cassandra_v4$cql_update
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__10 = RT.var((String)"datomic.cassandra-v4", (String)"update-stmt");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__12 = RT.classForName((String)"java.lang.Object");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__14 = RT.var((String)"datomic.cassandra-v4", (String)"updated?");

    public static Object invokeStatic(Object session, Object table, Object ensure_rev, Object p__10110, Object v_map) {
        ResultSet res;
        Object stmt;
        Object vec__10111;
        Object object = p__10110;
        p__10110 = null;
        Object object2 = vec__10111 = object;
        vec__10111 = null;
        Object seq__10112 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object first__10113 = ((IFn)const__1.getRawRoot()).invoke(seq__10112);
        Object object3 = seq__10112;
        seq__10112 = null;
        Object seq__101122 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__10113;
        first__10113 = null;
        Object id_key = object4;
        Object object5 = seq__101122;
        seq__101122 = null;
        Object ks = object5;
        Object id = RT.get((Object)v_map, (Object)id_key);
        Object object6 = v_map;
        v_map = null;
        Object object7 = ks;
        ks = null;
        Object col_vals = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(object6, object7));
        Object col_names = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), const__1.getRawRoot()), col_vals);
        Object object8 = table;
        table = null;
        Object object9 = id_key;
        id_key = null;
        Object object10 = col_names;
        col_names = null;
        Object object11 = stmt = ((IFn)const__10.getRawRoot()).invoke(session, object8, object9, object10);
        stmt = null;
        Object object12 = col_vals;
        col_vals = null;
        Object object13 = id;
        id = null;
        Object object14 = ensure_rev;
        ensure_rev = null;
        BoundStatement bound = ((PreparedStatement)object11).bind((Object[])((IFn)const__11.getRawRoot()).invoke(const__12, ((IFn)const__13.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__5.getRawRoot(), object12), (Object)Tuple.create((Object)object13, (Object)object14))));
        Object object15 = session;
        session = null;
        BoundStatement boundStatement = bound;
        bound = null;
        ResultSet resultSet = res = ((SyncCqlSession)object15).execute(((Statement)boundStatement).setSerialConsistencyLevel(ConsistencyLevel.SERIAL));
        res = null;
        return ((IFn)const__14.getRawRoot()).invoke((Object)resultSet);
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
        return cassandra_v4$cql_update.invokeStatic(object6, object7, object8, object9, object10);
    }
}


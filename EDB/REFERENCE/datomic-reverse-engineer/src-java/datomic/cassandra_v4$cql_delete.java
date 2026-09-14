/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
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
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.SyncCqlSession;

public final class cassandra_v4$cql_delete
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.cassandra-v4", (String)"delete-stmt");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__5 = RT.classForName((String)"java.lang.Object");

    public static Object invokeStatic(Object session, Object table, Object id, Object p__10131) {
        ResultSet res;
        Object stmt;
        Object vec__10132;
        Object object = p__10131;
        p__10131 = null;
        Object object2 = vec__10132 = object;
        vec__10132 = null;
        Object seq__10133 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object first__10134 = ((IFn)const__1.getRawRoot()).invoke(seq__10133);
        Object object3 = seq__10133;
        seq__10133 = null;
        Object seq__101332 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__10134;
        first__10134 = null;
        Object id_key = object4;
        seq__101332 = null;
        Object object5 = table;
        table = null;
        Object object6 = id_key;
        id_key = null;
        Object object7 = stmt = ((IFn)const__3.getRawRoot()).invoke(session, object5, object6);
        stmt = null;
        Object object8 = id;
        id = null;
        BoundStatement bs = ((PreparedStatement)object7).bind((Object[])((IFn)const__4.getRawRoot()).invoke(const__5, (Object)Tuple.create((Object)object8)));
        Object object9 = session;
        session = null;
        BoundStatement boundStatement = bs;
        bs = null;
        ResultSet resultSet = res = ((SyncCqlSession)object9).execute((Statement)boundStatement);
        res = null;
        return resultSet;
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
        return cassandra_v4$cql_delete.invokeStatic(object5, object6, object7, object8);
    }
}


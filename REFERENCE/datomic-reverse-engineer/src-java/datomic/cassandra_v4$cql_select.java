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
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;

public final class cassandra_v4$cql_select
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cassandra-v4", (String)"select-stmt");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-v4", (String)"select-with-consistency");
    public static final Var const__3 = RT.var((String)"datomic.cassandra-v4", (String)"row->map");

    public static Object invokeStatic(Object session, Object table, Object id, Object ks, Object consistent_QMARK_) {
        Object object;
        Object temp__5457__auto__10129;
        Object object2;
        Object or__5238__auto__10128;
        Object object3;
        Object and__5236__auto__10127;
        Object object4 = table;
        table = null;
        Object stmt = ((IFn)const__0.getRawRoot()).invoke(session, object4, ks);
        Object object5 = consistent_QMARK_;
        consistent_QMARK_ = null;
        Object object6 = and__5236__auto__10127 = ((IFn)const__1.getRawRoot()).invoke(object5);
        if (object6 != null && object6 != Boolean.FALSE) {
            object3 = ((IFn)const__2.getRawRoot()).invoke(session, stmt, (Object)ConsistencyLevel.ONE, (Object)Boolean.FALSE, id);
        } else {
            object3 = and__5236__auto__10127;
            and__5236__auto__10127 = null;
        }
        Object object7 = or__5238__auto__10128 = object3;
        if (object7 != null && object7 != Boolean.FALSE) {
            object2 = or__5238__auto__10128;
            or__5238__auto__10128 = null;
        } else {
            Object object8 = session;
            session = null;
            Object object9 = stmt;
            stmt = null;
            Object object10 = id;
            id = null;
            object2 = ((IFn)const__2.getRawRoot()).invoke(object8, object9, (Object)DefaultConsistencyLevel.LOCAL_QUORUM, (Object)Boolean.TRUE, object10);
        }
        Object object11 = temp__5457__auto__10129 = object2;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object row;
            Object object12 = temp__5457__auto__10129;
            temp__5457__auto__10129 = null;
            Object object13 = row = object12;
            row = null;
            Object object14 = ks;
            ks = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object13, object14);
        } else {
            object = null;
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
        return cassandra_v4$cql_select.invokeStatic(object6, object7, object8, object9, object10);
    }
}


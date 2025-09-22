/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.datastax.driver.core.PreparedStatement
 *  com.datastax.driver.core.ResultSet
 *  com.datastax.driver.core.Session
 *  com.datastax.driver.core.Statement
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public final class cassandra$cql_delete
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.cassandra", (String)"delete-stmt");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__5 = RT.classForName((String)"java.lang.Object");

    public static Object invokeStatic(Object session, Object table, Object id, Object p__17755) {
        ResultSet res;
        Object vec__17756;
        Object object = p__17755;
        p__17755 = null;
        Object object2 = vec__17756 = object;
        vec__17756 = null;
        Object seq__17757 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object first__17758 = ((IFn)const__1.getRawRoot()).invoke(seq__17757);
        Object object3 = seq__17757;
        seq__17757 = null;
        Object seq__177572 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__17758;
        first__17758 = null;
        Object id_key = object4;
        seq__177572 = null;
        Object object5 = table;
        table = null;
        Object object6 = id_key;
        id_key = null;
        Object stmt = ((IFn)const__3.getRawRoot()).invoke(session, object5, object6);
        Object object7 = session;
        session = null;
        Object object8 = stmt;
        stmt = null;
        Object object9 = id;
        id = null;
        ResultSet resultSet = res = ((Session)object7).execute((Statement)((PreparedStatement)object8).bind((Object[])((IFn)const__4.getRawRoot()).invoke(const__5, (Object)Tuple.create((Object)object9))));
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
        return cassandra$cql_delete.invokeStatic(object5, object6, object7, object8);
    }
}


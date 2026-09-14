/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.datastax.driver.core.BoundStatement
 *  com.datastax.driver.core.ConsistencyLevel
 *  com.datastax.driver.core.PreparedStatement
 *  com.datastax.driver.core.Session
 *  com.datastax.driver.core.Statement
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public final class cassandra$select_with_consistency
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__1 = RT.classForName((String)"java.lang.Object");

    public static Object invokeStatic(Object session, Object stmt, Object consistency, Object serial, Object id) {
        Object object = consistency;
        consistency = null;
        ((PreparedStatement)stmt).setConsistencyLevel((ConsistencyLevel)object);
        Object object2 = stmt;
        stmt = null;
        Object object3 = id;
        id = null;
        BoundStatement bound = ((PreparedStatement)object2).bind((Object[])((IFn)const__0.getRawRoot()).invoke(const__1, (Object)Tuple.create((Object)object3)));
        Object object4 = serial;
        serial = null;
        if (object4 != null && object4 != Boolean.FALSE) {
            ((Statement)bound).setSerialConsistencyLevel(ConsistencyLevel.SERIAL);
        }
        Object object5 = session;
        session = null;
        BoundStatement boundStatement = bound;
        bound = null;
        return ((Session)object5).execute((Statement)boundStatement).one();
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
        return cassandra$select_with_consistency.invokeStatic(object6, object7, object8, object9, object10);
    }
}


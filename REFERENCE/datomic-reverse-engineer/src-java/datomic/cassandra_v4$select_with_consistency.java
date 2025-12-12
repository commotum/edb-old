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
 *  com.datastax.oss.driver.api.core.PagingIterable
 *  com.datastax.oss.driver.api.core.cql.BoundStatement
 *  com.datastax.oss.driver.api.core.cql.PreparedStatement
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
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.cql.SyncCqlSession;

public final class cassandra_v4$select_with_consistency
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__1 = RT.classForName((String)"java.lang.Object");

    public static Object invokeStatic(Object session, Object stmt, Object consistency, Object serial, Object id) {
        Statement statement;
        BoundStatement bound;
        Object object = stmt;
        stmt = null;
        Object object2 = id;
        id = null;
        BoundStatement boundStatement = bound = ((PreparedStatement)object).bind((Object[])((IFn)const__0.getRawRoot()).invoke(const__1, (Object)Tuple.create((Object)object2)));
        bound = null;
        Object object3 = consistency;
        consistency = null;
        Statement bound2 = ((Statement)boundStatement).setConsistencyLevel((ConsistencyLevel)object3);
        Object object4 = serial;
        serial = null;
        if (object4 != null && object4 != Boolean.FALSE) {
            Statement statement2 = bound2;
            bound2 = null;
            statement = statement2.setSerialConsistencyLevel(ConsistencyLevel.SERIAL);
        } else {
            statement = bound2;
            bound2 = null;
        }
        Statement bound3 = statement;
        Object object5 = session;
        session = null;
        Statement statement3 = bound3;
        bound3 = null;
        return ((PagingIterable)((SyncCqlSession)object5).execute(statement3)).one();
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
        return cassandra_v4$select_with_consistency.invokeStatic(object6, object7, object8, object9, object10);
    }
}


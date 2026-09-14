/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datastax.driver.core.PreparedStatement
 *  com.datastax.driver.core.Session
 *  com.datastax.driver.core.policies.RetryPolicy
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RetryPolicy;

public final class cassandra$select_stmt_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cassandra", (String)"select-string");
    public static final Var const__1 = RT.var((String)"datomic.cassandra", (String)"retry-policy");

    public static Object invokeStatic(Object session, Object table, Object ks) {
        PreparedStatement stmt;
        Object object = session;
        session = null;
        Object object2 = table;
        table = null;
        Object object3 = ks;
        ks = null;
        PreparedStatement preparedStatement = stmt = ((Session)object).prepare((String)((IFn)const__0.getRawRoot()).invoke(object2, object3));
        stmt = null;
        return preparedStatement.setRetryPolicy((RetryPolicy)((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cassandra$select_stmt_STAR_.invokeStatic(object4, object5, object6);
    }
}


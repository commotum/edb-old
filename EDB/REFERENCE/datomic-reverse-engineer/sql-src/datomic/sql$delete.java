/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class sql$delete
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"connect");

    public static Object invokeStatic(Object spec, Object id) {
        Integer n;
        Object object = spec;
        spec = null;
        Object conn = ((IFn)const__0.getRawRoot()).invoke(object);
        try {
            Integer n2;
            PreparedStatement stmt = ((Connection)conn).prepareStatement("delete from datomic_kvs where id = ?");
            try {
                Object object2 = id;
                id = null;
                stmt.setObject(RT.intCast((long)1L), object2);
                n2 = stmt.executeUpdate();
            }
            finally {
                PreparedStatement preparedStatement = stmt;
                stmt = null;
                ((Statement)preparedStatement).close();
            }
            n = n2;
        }
        finally {
            Object object3 = conn;
            conn = null;
            ((Connection)object3).close();
        }
        return n;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return sql$delete.invokeStatic(object3, object4);
    }
}

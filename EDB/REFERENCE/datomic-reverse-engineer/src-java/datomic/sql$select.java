/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class sql$select
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"connect");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
    public static final Keyword const__3 = RT.keyword(null, (String)"rev");
    public static final Keyword const__4 = RT.keyword(null, (String)"map");
    public static final Keyword const__5 = RT.keyword(null, (String)"val");

    public static Object invokeStatic(Object spec, Object id) {
        IPersistentMap iPersistentMap;
        Object object = spec;
        spec = null;
        Object conn = ((IFn)const__0.getRawRoot()).invoke(object);
        try {
            IPersistentMap iPersistentMap2;
            PreparedStatement stmt = ((Connection)conn).prepareStatement("select id, rev, map, val from datomic_kvs where id = ?");
            try {
                IPersistentMap iPersistentMap3;
                Object object2 = id;
                id = null;
                stmt.setObject(RT.intCast((long)1L), object2);
                ResultSet rs = stmt.executeQuery();
                try {
                    iPersistentMap3 = rs.next() ? RT.mapUniqueKeys((Object[])new Object[]{const__2, rs.getString("id"), const__3, Numbers.num((long)rs.getLong("rev")), const__4, rs.getString("map"), const__5, rs.getBytes("val")}) : null;
                }
                finally {
                    ResultSet resultSet = rs;
                    rs = null;
                    resultSet.close();
                }
                iPersistentMap2 = iPersistentMap3;
            }
            finally {
                PreparedStatement preparedStatement = stmt;
                stmt = null;
                ((Statement)preparedStatement).close();
            }
            iPersistentMap = iPersistentMap2;
        }
        finally {
            Object object3 = conn;
            conn = null;
            ((Connection)object3).close();
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return sql$select.invokeStatic(object3, object4);
    }
}


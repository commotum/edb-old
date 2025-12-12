/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;

public final class sql$update_with_nulls
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"rev");
    public static final Keyword const__4 = RT.keyword(null, (String)"map");
    public static final Keyword const__5 = RT.keyword(null, (String)"val");
    public static final Var const__6 = RT.var((String)"datomic.sql", (String)"connect");

    public static Object invokeStatic(Object spec, Object id, Object ensure_rev, Object p__11484) {
        Integer n;
        Object object;
        Object object2 = p__11484;
        p__11484 = null;
        Object map__11485 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__11485);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__11485;
            map__11485 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__11485;
            map__11485 = null;
        }
        Object map__114852 = object;
        Object rev = RT.get((Object)map__114852, (Object)const__3);
        Object map = RT.get((Object)map__114852, (Object)const__4);
        Object object5 = map__114852;
        map__114852 = null;
        Object val = RT.get((Object)object5, (Object)const__5);
        Object object6 = spec;
        spec = null;
        Object conn = ((IFn)const__6.getRawRoot()).invoke(object6);
        try {
            Integer n2;
            String update_sql;
            String string = update_sql = "update datomic_kvs set rev=?, map=?, val=? where id=? and rev=?";
            update_sql = null;
            PreparedStatement stmt = ((Connection)conn).prepareStatement(string);
            try {
                Object object7 = rev;
                rev = null;
                stmt.setObject(RT.intCast((long)1L), object7, Types.INTEGER);
                Object object8 = map;
                map = null;
                stmt.setObject(RT.intCast((long)2L), object8, Types.LONGVARCHAR);
                Object object9 = val;
                val = null;
                stmt.setObject(RT.intCast((long)3L), object9, Types.LONGVARBINARY);
                Object object10 = id;
                id = null;
                stmt.setObject(RT.intCast((long)4L), object10, Types.VARCHAR);
                Object object11 = ensure_rev;
                ensure_rev = null;
                stmt.setObject(RT.intCast((long)5L), object11, Types.INTEGER);
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
            Object object12 = conn;
            conn = null;
            ((Connection)object12).close();
        }
        return n;
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
        return sql$update_with_nulls.invokeStatic(object5, object6, object7, object8);
    }
}

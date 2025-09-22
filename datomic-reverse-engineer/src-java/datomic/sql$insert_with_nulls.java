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

public final class sql$insert_with_nulls
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Keyword const__5 = RT.keyword(null, (String)"map");
    public static final Keyword const__6 = RT.keyword(null, (String)"val");
    public static final Var const__7 = RT.var((String)"datomic.sql", (String)"connect");

    public static Object invokeStatic(Object spec, Object p__11487) {
        Integer n;
        Object object;
        Object object2 = p__11487;
        p__11487 = null;
        Object map__11488 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__11488);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__11488;
            map__11488 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__11488;
            map__11488 = null;
        }
        Object map__114882 = object;
        Object id = RT.get((Object)map__114882, (Object)const__3);
        Object rev = RT.get((Object)map__114882, (Object)const__4);
        Object map2 = RT.get((Object)map__114882, (Object)const__5);
        Object object5 = map__114882;
        map__114882 = null;
        Object val = RT.get((Object)object5, (Object)const__6);
        Object object6 = spec;
        spec = null;
        Object conn = ((IFn)const__7.getRawRoot()).invoke(object6);
        try {
            Integer n2;
            String insert_sql;
            String string = insert_sql = "insert into datomic_kvs (id, rev, map, val) values (?, ?, ?, ?)";
            insert_sql = null;
            PreparedStatement stmt = ((Connection)conn).prepareStatement(string);
            try {
                Object object7 = id;
                id = null;
                stmt.setObject(RT.intCast((long)1L), object7, Types.LONGVARCHAR);
                Object object8 = rev;
                rev = null;
                stmt.setObject(RT.intCast((long)2L), object8, Types.INTEGER);
                Object object9 = map2;
                map2 = null;
                stmt.setObject(RT.intCast((long)3L), object9, Types.LONGVARCHAR);
                Object object10 = val;
                val = null;
                stmt.setObject(RT.intCast((long)4L), object10, Types.LONGVARBINARY);
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
            Object object11 = conn;
            conn = null;
            ((Connection)object11).close();
        }
        return n;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return sql$insert_with_nulls.invokeStatic(object3, object4);
    }
}


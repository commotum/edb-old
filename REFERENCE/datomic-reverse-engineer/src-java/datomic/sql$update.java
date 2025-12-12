/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.sql$update$fn__11491;
import datomic.sql$update$fn__11497;
import datomic.sql$update$fn__11502;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class sql$update
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"connect");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__6 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"));
    public static final Var const__8 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"map-indexed");

    public static Object invokeStatic(Object spec, Object id, Object ensure_rev, Object v_map) {
        Integer n;
        Object object = spec;
        spec = null;
        Object conn = ((IFn)const__0.getRawRoot()).invoke(object);
        try {
            Integer n2;
            Object update_sql;
            Object col_str;
            Object object2 = v_map;
            v_map = null;
            Object col_vals = ((IFn)const__1.getRawRoot()).invoke((Object)new sql$update$fn__11491(), ((IFn)const__2.getRawRoot()).invoke(object2, (Object)const__6));
            int col_num = RT.count((Object)col_vals);
            Object object3 = col_str = ((IFn)const__8.getRawRoot()).invoke((Object)", ", ((IFn)const__9.getRawRoot()).invoke((Object)new sql$update$fn__11497(), col_vals));
            col_str = null;
            Object object4 = update_sql = ((IFn)const__10.getRawRoot()).invoke((Object)"update datomic_kvs set ", object3, (Object)" where id=? and rev=?");
            update_sql = null;
            PreparedStatement stmt = ((Connection)conn).prepareStatement((String)object4);
            try {
                Object object5 = col_vals;
                col_vals = null;
                ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)new sql$update$fn__11502(stmt), object5));
                Object object6 = id;
                id = null;
                stmt.setObject(RT.intCast((long)Numbers.add((long)col_num, (long)1L)), object6);
                Object object7 = ensure_rev;
                ensure_rev = null;
                stmt.setObject(RT.intCast((long)Numbers.add((long)col_num, (long)2L)), object7);
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
            Object object8 = conn;
            conn = null;
            ((Connection)object8).close();
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
        return sql$update.invokeStatic(object5, object6, object7, object8);
    }
}


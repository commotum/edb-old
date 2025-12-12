/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.sql$insert$fn__11506;
import datomic.sql$insert$fn__11512;
import datomic.sql$insert$fn__11517;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class sql$insert
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"connect");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"));
    public static final Var const__9 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"repeatedly");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"map-indexed");

    public static Object invokeStatic(Object spec, Object v_map) {
        Integer n;
        Object object = spec;
        spec = null;
        Object conn = ((IFn)const__0.getRawRoot()).invoke(object);
        try {
            Integer n2;
            Object insert_sql;
            Object object2 = v_map;
            v_map = null;
            Object col_vals = ((IFn)const__1.getRawRoot()).invoke((Object)new sql$insert$fn__11506(), ((IFn)const__2.getRawRoot()).invoke(object2, (Object)const__7));
            int col_num = RT.count((Object)col_vals);
            Object col_str = ((IFn)const__9.getRawRoot()).invoke((Object)", ", ((IFn)const__10.getRawRoot()).invoke((Object)new sql$insert$fn__11512(), col_vals));
            Object col_placeholders = ((IFn)const__9.getRawRoot()).invoke((Object)", ", ((IFn)const__11.getRawRoot()).invoke((Object)col_num, ((IFn)const__12.getRawRoot()).invoke((Object)"?")));
            Object object3 = col_str;
            col_str = null;
            Object object4 = col_placeholders;
            col_placeholders = null;
            Object object5 = insert_sql = ((IFn)const__13.getRawRoot()).invoke((Object)"insert into datomic_kvs (", object3, (Object)") values (", object4, (Object)")");
            insert_sql = null;
            PreparedStatement stmt = ((Connection)conn).prepareStatement((String)object5);
            try {
                Object object6 = col_vals;
                col_vals = null;
                ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)new sql$insert$fn__11517(stmt), object6));
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
            Object object7 = conn;
            conn = null;
            ((Connection)object7).close();
        }
        return n;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return sql$insert.invokeStatic(object3, object4);
    }
}

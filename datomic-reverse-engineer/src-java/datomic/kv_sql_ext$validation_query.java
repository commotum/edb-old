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

public final class kv_sql_ext$validation_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.kv-sql-ext", (String)"validation-query*");

    public static Object invokeStatic(Object provider2) {
        Object object;
        Object or__5238__auto__11551;
        Object object2 = or__5238__auto__11551 = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.sqlValidationQuery");
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__11551;
            or__5238__auto__11551 = null;
        } else {
            Object object3 = provider2;
            provider2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$validation_query.invokeStatic(object2);
    }
}


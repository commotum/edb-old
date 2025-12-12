/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.IDb;

public final class db$resolve_id
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Keyword const__3 = RT.keyword(null, (String)"else");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"extended-resolve-id");

    public static Object invokeStatic(Object db2, Object x) {
        Object object;
        if (x instanceof Long) {
            object = x;
            x = null;
        } else {
            Object object2 = ((IFn)const__2.getRawRoot()).invoke(x);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = db2;
                db2 = null;
                Object object4 = x;
                x = null;
                object = ((IDb)object3).idOf(object4);
            } else {
                Keyword keyword = const__3;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object5 = db2;
                    db2 = null;
                    Object object6 = x;
                    x = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object5, object6);
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$resolve_id.invokeStatic(object3, object4);
    }
}


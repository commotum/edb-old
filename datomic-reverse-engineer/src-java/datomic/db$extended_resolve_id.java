/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.DbId;
import datomic.db.IDb;
import java.util.List;

public final class db$extended_resolve_id
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-dbid");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"resolve-lookup-ref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Keyword const__7 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object db2, Object x) {
        Object object;
        if (x instanceof DbId) {
            Object object2 = db2;
            db2 = null;
            Object object3 = x;
            x = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, object3);
        } else if (x instanceof List) {
            Object object4 = db2;
            db2 = null;
            Object object5 = x;
            x = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object4, object5);
        } else {
            Object object6 = ((IFn)const__5.getRawRoot()).invoke(x);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = x;
                x = null;
                object = Numbers.num((long)RT.longCast((Object)object7));
            } else {
                Keyword keyword = const__7;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object8 = db2;
                    db2 = null;
                    Object object9 = x;
                    x = null;
                    object = ((IDb)object8).idOf(object9);
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
        return db$extended_resolve_id.invokeStatic(object3, object4);
    }
}


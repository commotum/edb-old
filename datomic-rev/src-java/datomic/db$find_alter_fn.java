/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$find_alter_fn$fn__13207;
import datomic.db$find_alter_fn$fn__13209;
import datomic.db$find_alter_fn$fn__13213;

public final class db$find_alter_fn
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__2 = RT.keyword((String)"db.attr", (String)"preds");
    public static final Keyword const__3 = RT.keyword(null, (String)"default");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"alter-fns");

    public static Object invokeStatic(Object db2, Object eid, Object fid, Object from, Object to) {
        Object object;
        if (Util.equiv((Object)from, (Object)to)) {
            object = new db$find_alter_fn$fn__13207();
        } else {
            Object object2 = db2;
            db2 = null;
            if (Util.equiv((Object)fid, (Object)((IFn)const__1.getRawRoot()).invoke(object2, (Object)const__2))) {
                object = new db$find_alter_fn$fn__13209();
            } else {
                Keyword keyword = const__3;
                if (keyword != null && keyword != Boolean.FALSE) {
                    IPersistentVector iPersistentVector = Tuple.create((Object)fid, (Object)from, (Object)to);
                    Object object3 = to;
                    to = null;
                    Object object4 = from;
                    from = null;
                    Object object5 = fid;
                    fid = null;
                    Object object6 = eid;
                    eid = null;
                    object = RT.get((Object)const__5.getRawRoot(), (Object)iPersistentVector, (Object)((Object)new db$find_alter_fn$fn__13213(object3, object4, object5, object6)));
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$find_alter_fn.invokeStatic(object6, object7, object8, object9, object10);
    }
}


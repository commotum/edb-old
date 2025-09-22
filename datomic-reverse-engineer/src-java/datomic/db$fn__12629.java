/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.LocalizeTempid;
import java.util.Map;

public final class db$fn__12629
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object this_, Object db2, Object procargs, Object local_tempids) {
        Object temp__5455__auto__12631;
        Object object;
        if (((String)this_).startsWith(":")) {
            Object object2 = this_;
            this_ = null;
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(object2);
            if (Util.classOf((Object)object3) != __cached_class__0) {
                if (object3 instanceof LocalizeTempid) {
                    Object object4 = db2;
                    db2 = null;
                    Object object5 = procargs;
                    procargs = null;
                    Object object6 = local_tempids;
                    local_tempids = null;
                    object = ((LocalizeTempid)object3).local_id(object4, object5, object6);
                    return object;
                }
                object3 = object3;
                __cached_class__0 = Util.classOf((Object)object3);
            }
            Object object7 = db2;
            db2 = null;
            Object object8 = procargs;
            procargs = null;
            Object object9 = local_tempids;
            local_tempids = null;
            object = const__0.getRawRoot().invoke(object3, object7, object8, object9);
            return object;
        }
        Object object10 = temp__5455__auto__12631 = RT.get((Object)local_tempids, (Object)this_);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object id;
            Object object11 = temp__5455__auto__12631;
            temp__5455__auto__12631 = null;
            object = id = object11;
            return object;
        }
        Object local_id = ((IFn.LO)const__3.getRawRoot()).invokePrim(Util.equiv((Object)this_, (Object)"datomic.tx") ? 3L : 16L);
        Object object12 = local_tempids;
        local_tempids = null;
        Object object13 = this_;
        this_ = null;
        ((Map)object12).put(object13, local_id);
        object = local_id;
        return object;
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
        return db$fn__12629.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"local-id");
        const__1 = RT.var((String)"datomic.db", (String)"to-kw");
        const__3 = RT.var((String)"datomic.db", (String)"tempid");
    }
}


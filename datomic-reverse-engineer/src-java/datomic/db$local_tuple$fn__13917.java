/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class db$local_tuple$fn__13917
extends AFunction {
    Object procargs;
    Object ref_offset_QMARK_;
    Object db;
    Object local_tempids;
    private static Class __cached_class__0;
    public static final Var const__1;

    public db$local_tuple$fn__13917(Object object, Object object2, Object object3, Object object4) {
        this.procargs = object;
        this.ref_offset_QMARK_ = object2;
        this.db = object3;
        this.local_tempids = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object idx, Object elem) {
        Object object;
        if (Util.identical((Object)elem, null)) {
            return null;
        }
        Object object2 = idx;
        idx = null;
        Object object3 = ((IFn)this_.ref_offset_QMARK_).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = elem;
            elem = null;
            Object object5 = object4;
            if (Util.classOf((Object)object4) != __cached_class__0) {
                if (object5 instanceof LocalizeTempid) {
                    object = ((LocalizeTempid)object5).local_id(this_.db, this_.procargs, this_.local_tempids);
                    return object;
                }
                object5 = object5;
                __cached_class__0 = Util.classOf((Object)object5);
            }
            db$local_tuple$fn__13917 this_ = null;
            object = const__1.getRawRoot().invoke(object5, this_.db, this_.procargs, this_.local_tempids);
            return object;
        }
        object = elem;
        return object;
    }

    static {
        const__1 = RT.var((String)"datomic.db", (String)"local-id");
    }
}


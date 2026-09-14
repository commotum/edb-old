/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class integrity$crosscheck_log_representations$fn__22167
extends AFunction {
    Object progress;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"id");

    public integrity$crosscheck_log_representations$fn__22167(Object object) {
        this.progress = object;
    }

    public Object invoke(Object p__22166) {
        Object object = p__22166;
        p__22166 = null;
        Object vec__22168 = object;
        Object a = RT.nth((Object)vec__22168, (int)RT.intCast((long)0L), null);
        Object object2 = vec__22168;
        vec__22168 = null;
        Object b = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        ((IFn)this_.progress).invoke();
        Object object3 = a;
        a = null;
        Object object4 = b;
        b = null;
        integrity$crosscheck_log_representations$fn__22167 this_ = null;
        return Util.equiv((Object)((IFn)const__4.getRawRoot()).invoke(object3, (Object)const__5), (Object)((IFn)const__4.getRawRoot()).invoke(object4, (Object)const__5)) ? Boolean.TRUE : Boolean.FALSE;
    }
}


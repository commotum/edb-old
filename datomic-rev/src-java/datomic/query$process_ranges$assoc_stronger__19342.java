/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class query$process_ranges$assoc_stronger__19342
extends AFunction {
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"=");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object k, Object v, Object cmp) {
        Object object;
        boolean bl;
        Object ev;
        Object object2 = ev = RT.get((Object)m, (Object)k);
        ev = null;
        boolean or__5238__auto__19344 = Util.identical((Object)object2, null);
        if (or__5238__auto__19344) {
            bl = or__5238__auto__19344;
        } else {
            Object object3 = cmp;
            cmp = null;
            bl = Util.equiv((Object)const__3, (Object)object3);
        }
        if (bl) {
            Object object4 = m;
            m = null;
            Object object5 = k;
            k = null;
            Object object6 = v;
            v = null;
            query$process_ranges$assoc_stronger__19342 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object4, object5, object6);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}


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

public final class cli$coerce_vals$fn__20718
extends AFunction {
    Object idx;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"identity");

    public cli$coerce_vals$fn__20718(Object object) {
        this.idx = object;
    }

    public Object invoke(Object m, Object p__20717) {
        Object object = p__20717;
        p__20717 = null;
        Object vec__20719 = object;
        Object k = RT.nth((Object)vec__20719, (int)RT.intCast((long)0L), null);
        Object object2 = vec__20719;
        vec__20719 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = k;
        Object object5 = k;
        k = null;
        Object object6 = v;
        v = null;
        cli$coerce_vals$fn__20718 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, object4, ((IFn)RT.get((Object)this_.idx, (Object)object5, (Object)const__5.getRawRoot())).invoke(object6));
    }
}


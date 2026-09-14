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

public final class datalog$bound_consts$fn__18856
extends AFunction {
    Object srcs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vector?");

    public datalog$bound_consts$fn__18856(Object object) {
        this.srcs = object;
    }

    public Object invoke(Object m, Object v, Object b) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = m;
        m = null;
        Object object3 = v;
        v = null;
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(b);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = b;
            b = null;
            Object vec__18857 = object5;
            Object src = RT.nth((Object)vec__18857, (int)RT.uncheckedIntCast((long)0L), null);
            Object object6 = vec__18857;
            vec__18857 = null;
            Object idx = RT.nth((Object)object6, (int)RT.uncheckedIntCast((long)1L), null);
            Object object7 = src;
            src = null;
            Object object8 = idx;
            idx = null;
            object = RT.nth((Object)RT.get((Object)this_.srcs, (Object)object7), (int)RT.uncheckedIntCast((Object)((Number)object8)));
        } else {
            Object object9 = b;
            b = null;
            object = RT.get((Object)this_.srcs, (Object)object9);
        }
        datalog$bound_consts$fn__18856 this_ = null;
        return iFn.invoke(object2, object3, object);
    }
}


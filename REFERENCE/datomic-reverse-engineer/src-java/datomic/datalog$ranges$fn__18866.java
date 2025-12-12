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
import datomic.datalog$ranges$fn__18866$fn__18870;

public final class datalog$ranges$fn__18866
extends AFunction {
    Object in_consts;
    Object cmps;
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");

    public datalog$ranges$fn__18866(Object object, Object object2) {
        this.in_consts = object;
        this.cmps = object2;
    }

    public Object invoke(Object m, Object v, Object p__18865) {
        Object object;
        Object object2 = p__18865;
        p__18865 = null;
        Object vec__18867 = object2;
        Object cmpsym = RT.nth((Object)vec__18867, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__18867;
        vec__18867 = null;
        Object cb = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(cb);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = cb;
            cb = null;
            object = RT.get((Object)this_.in_consts, (Object)object5);
        } else {
            object = cb;
            cb = null;
        }
        Object c = object;
        Object object6 = cmpsym;
        cmpsym = null;
        Object cmp = ((IFn)this_.cmps).invoke(object6);
        Object object7 = m;
        m = null;
        Object object8 = v;
        v = null;
        Object object9 = c;
        c = null;
        Object object10 = cmp;
        cmp = null;
        datalog$ranges$fn__18866 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(object7, object8, (Object)new datalog$ranges$fn__18866$fn__18870(object9, object10));
    }
}


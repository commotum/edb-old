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

public final class config$reset_BANG_$fn__840
extends AFunction {
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__6 = RT.var((String)"datomic.config", (String)"read-system-property");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"fn?");

    public Object invoke(Object m, Object p__839) {
        Object object;
        Object object2 = p__839;
        p__839 = null;
        Object vec__841 = object2;
        Object prop = RT.nth((Object)vec__841, (int)RT.intCast((long)0L), null);
        Object coerce = RT.nth((Object)vec__841, (int)RT.intCast((long)1L), null);
        Object valid_QMARK_ = RT.nth((Object)vec__841, (int)RT.intCast((long)2L), null);
        Object object3 = vec__841;
        vec__841 = null;
        Object object4 = RT.nth((Object)object3, (int)RT.intCast((long)3L), null);
        IFn iFn = (IFn)const__5.getRawRoot();
        Object object5 = prop;
        IFn iFn2 = (IFn)const__6.getRawRoot();
        Object object6 = prop;
        prop = null;
        Object object7 = coerce;
        coerce = null;
        Object object8 = valid_QMARK_;
        valid_QMARK_ = null;
        Object object9 = ((IFn)const__7.getRawRoot()).invoke(object4);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = object4;
            object4 = null;
            Object object11 = m;
            m = null;
            object = ((IFn)object10).invoke(object11);
        } else {
            object = object4;
            object4 = null;
        }
        config$reset_BANG_$fn__840 this_ = null;
        return iFn.invoke(m, object5, iFn2.invoke(object6, object7, object8, object));
    }
}


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
import java.lang.reflect.Method;

public final class datafy$fn__17211$fn__17212
extends AFunction {
    Object c;
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"setter-name->keyword");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__2 = RT.var((String)"datomic.datafy", (String)"property-descriptor");

    public datafy$fn__17211$fn__17212(Object object) {
        this.c = object;
    }

    public Object invoke(Object m, Object meth) {
        Object k = ((IFn)const__0.getRawRoot()).invoke((Object)((Method)meth).getName());
        Object object = m;
        m = null;
        Object object2 = k;
        Object object3 = k;
        k = null;
        Object object4 = meth;
        meth = null;
        datafy$fn__17211$fn__17212 this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, object2, ((IFn)const__2.getRawRoot()).invoke(this_.c, object3, RT.nth(((Method)object4).getParameterTypes(), (int)RT.intCast((long)0L))));
    }
}


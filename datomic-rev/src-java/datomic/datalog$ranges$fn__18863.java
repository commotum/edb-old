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

public final class datalog$ranges$fn__18863
extends AFunction {
    Object in_consts;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"variable?");

    public datalog$ranges$fn__18863(Object object) {
        this.in_consts = object;
    }

    public Object invoke(Object m, Object v, Object c) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = m;
        m = null;
        Object object3 = v;
        v = null;
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(c);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = c;
            c = null;
            object = RT.get((Object)this_.in_consts, (Object)object5);
        } else {
            object = c;
            c = null;
        }
        datalog$ranges$fn__18863 this_ = null;
        return iFn.invoke(object2, object3, object);
    }
}


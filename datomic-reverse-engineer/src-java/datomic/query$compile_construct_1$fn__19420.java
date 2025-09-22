/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class query$compile_construct_1$fn__19420
extends AFunction {
    Object smap;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"get");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"tuple");

    public query$compile_construct_1$fn__19420(Object object) {
        this.smap = object;
    }

    public Object invoke(Object x) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object pos;
            Object object3 = x;
            x = null;
            Object object4 = pos = RT.get((Object)this_.smap, (Object)object3);
            pos = null;
            query$compile_construct_1$fn__19420 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__5), ((IFn)const__4.getRawRoot()).invoke((Object)const__6), ((IFn)const__4.getRawRoot()).invoke(object4)));
        } else {
            object = x;
            Object var1_1 = null;
        }
        return object;
    }
}


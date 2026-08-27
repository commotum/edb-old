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
import datomic.index$merge_one_index$fn__15454$fn__15455;

public final class index$merge_one_index$fn__15454
extends AFunction {
    Object index;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.excise", (String)"datoms");

    public index$merge_one_index$fn__15454(Object object) {
        this.index = object;
    }

    public Object invoke(Object xsegs, Object p) {
        Object object = xsegs;
        xsegs = null;
        Object object2 = p;
        p = null;
        index$merge_one_index$fn__15454 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke((Object)new index$merge_one_index$fn__15454$fn__15455(this_.index), ((IFn)const__4.getRawRoot()).invoke(object2))));
    }
}


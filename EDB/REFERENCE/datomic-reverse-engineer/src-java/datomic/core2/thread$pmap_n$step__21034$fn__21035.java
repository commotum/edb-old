/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class thread$pmap_n$step__21034$fn__21035
extends AFunction {
    Object cs;
    Object step;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");

    public thread$pmap_n$step__21034$fn__21035(Object object, Object object2) {
        this.cs = object;
        this.step = object2;
    }

    public Object invoke() {
        Object object;
        this_.cs = null;
        Object ss = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), this_.cs);
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ss);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(const__5.getRawRoot(), ss);
            Object object4 = ss;
            ss = null;
            thread$pmap_n$step__21034$fn__21035 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object3, ((IFn)this_.step).invoke(((IFn)const__0.getRawRoot()).invoke(const__6.getRawRoot(), object4)));
        } else {
            object = null;
        }
        return object;
    }
}


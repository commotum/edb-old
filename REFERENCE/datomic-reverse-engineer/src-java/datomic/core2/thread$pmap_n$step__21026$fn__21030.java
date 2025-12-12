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

public final class thread$pmap_n$step__21026$fn__21030
extends AFunction {
    Object xs;
    Object fs;
    Object step;
    Object vs;
    Object x;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");

    public thread$pmap_n$step__21026$fn__21030(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.xs = object;
        this.fs = object2;
        this.step = object3;
        this.vs = object4;
        this.x = object5;
    }

    public Object invoke() {
        Object object;
        thread$pmap_n$step__21026$fn__21030 this_;
        Object temp__5802__auto__21032;
        this_.fs = null;
        Object object2 = temp__5802__auto__21032 = ((IFn)const__0.getRawRoot()).invoke(this_.fs);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5802__auto__21032;
            temp__5802__auto__21032 = null;
            Object s = object3;
            this_.x = null;
            this_.xs = null;
            Object object4 = s;
            s = null;
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this_.x), ((IFn)this_.step).invoke(this_.xs, ((IFn)const__3.getRawRoot()).invoke(object4)));
        } else {
            this_.vs = null;
            this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(const__2.getRawRoot(), this_.vs);
        }
        return object;
    }
}


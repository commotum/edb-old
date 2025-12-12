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
import datomic.index$fully_partition_by$fpb__15365$fn__15366$fn__15367;

public final class index$fully_partition_by$fpb__15365$fn__15366
extends AFunction {
    Object pcoll;
    Object fpb;
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"fully-take-while-delivering-tail");

    public index$fully_partition_by$fpb__15365$fn__15366(Object object, Object object2, Object object3) {
        this.pcoll = object;
        this.fpb = object2;
        this.f = object3;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__15370;
        Object coll;
        this_.pcoll = null;
        Object object2 = coll = ((IFn)const__0.getRawRoot()).invoke(this_.pcoll);
        coll = null;
        Object object3 = temp__5457__auto__15370 = ((IFn)const__1.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object fst;
            Object object4 = temp__5457__auto__15370;
            temp__5457__auto__15370 = null;
            Object s = object4;
            Object object5 = fst = ((IFn)const__2.getRawRoot()).invoke(s);
            fst = null;
            Object fv = ((IFn)this_.f).invoke(object5);
            Object pcoll = ((IFn)const__3.getRawRoot()).invoke();
            Object object6 = fv;
            fv = null;
            Object object7 = s;
            s = null;
            Object object8 = ((IFn)const__5.getRawRoot()).invoke(pcoll, (Object)new index$fully_partition_by$fpb__15365$fn__15366$fn__15367(object6, this_.f), object7);
            this_.f = null;
            Object object9 = pcoll;
            pcoll = null;
            index$fully_partition_by$fpb__15365$fn__15366 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object8, ((IFn)this_.fpb).invoke(this_.f, object9));
        } else {
            object = null;
        }
        return object;
    }
}


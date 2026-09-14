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

public final class fulltext$build_index$fn__14674$fn__14684
extends AFunction {
    Object aevt;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"scan-aevt");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");

    public fulltext$build_index$fn__14674$fn__14684(Object object) {
        this.aevt = object;
    }

    public Object invoke(Object m, Object attrid) {
        Object object;
        Object temp__5455__auto__14686;
        Object object2 = temp__5455__auto__14686 = ((IFn)const__0.getRawRoot()).invoke(this_.aevt, attrid);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__14686;
            temp__5455__auto__14686 = null;
            Object iter2 = object3;
            Object object4 = m;
            m = null;
            Object object5 = attrid;
            attrid = null;
            Object object6 = iter2;
            iter2 = null;
            fulltext$build_index$fn__14674$fn__14684 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object4, object5, object6);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}


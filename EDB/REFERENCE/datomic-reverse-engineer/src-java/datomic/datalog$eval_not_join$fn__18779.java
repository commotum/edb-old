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

public final class datalog$eval_not_join$fn__18779
extends AFunction {
    Object uvs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"gensym");

    public datalog$eval_not_join$fn__18779(Object object) {
        this.uvs = object;
    }

    public Object invoke(Object p1__18773_SHARP_) {
        Object object;
        Object or__5238__auto__18781;
        Object object2 = p1__18773_SHARP_;
        p1__18773_SHARP_ = null;
        Object object3 = or__5238__auto__18781 = ((IFn)this_.uvs).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__18781;
            or__5238__auto__18781 = null;
        } else {
            datalog$eval_not_join$fn__18779 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)"?nb__");
        }
        return object;
    }
}


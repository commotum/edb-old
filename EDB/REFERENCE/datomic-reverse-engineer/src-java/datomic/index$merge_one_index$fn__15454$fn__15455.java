/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index.IIndex;

public final class index$merge_one_index$fn__15454$fn__15455
extends AFunction {
    Object index;
    private static Class __cached_class__0;
    public static final Var const__0;

    public index$merge_one_index$fn__15454$fn__15455(Object object) {
        this.index = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__15441_SHARP_) {
        Object object;
        Object object2 = this_.index;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof IIndex) {
                Object object3 = p1__15441_SHARP_;
                p1__15441_SHARP_ = null;
                object = ((IIndex)object2).seek_seg(object3);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = p1__15441_SHARP_;
        p1__15441_SHARP_ = null;
        index$merge_one_index$fn__15454$fn__15455 this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.index", (String)"seek-seg");
    }
}


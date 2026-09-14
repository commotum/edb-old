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
import datomic.integrity$dir_seg_info_seq$fn__22229;

public final class integrity$dir_seg_info_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object dir, Object olookup) {
        Object object = olookup;
        olookup = null;
        Object object2 = dir;
        dir = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new integrity$dir_seg_info_seq$fn__22229(object), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$dir_seg_info_seq.invokeStatic(object3, object4);
    }
}


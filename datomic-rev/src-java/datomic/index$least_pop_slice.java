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
import datomic.index$least_pop_slice$fn__15647;
import datomic.index$least_pop_slice$get_slice__15649;

public final class index$least_pop_slice
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"min-key");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"last");

    public static Object invokeStatic(Object olookup, Object ks, Object index2, Object dir_partition_size2, Object slice_size) {
        Object object;
        Object object2 = index2;
        index2 = null;
        Object object3 = dir_partition_size2;
        dir_partition_size2 = null;
        Object object4 = olookup;
        olookup = null;
        Object object5 = ks;
        ks = null;
        Object offsets = ((IFn)const__0.getRawRoot()).invoke((Object)new index$least_pop_slice$fn__15647(object2, object3, object4), object5);
        index$least_pop_slice$get_slice__15649 get_slice = new index$least_pop_slice$get_slice__15649();
        Object object6 = slice_size;
        slice_size = null;
        Object pops = ((IFn)const__0.getRawRoot()).invoke((Object)get_slice, offsets, ((IFn)const__1.getRawRoot()).invoke(object6, offsets));
        Object object7 = ((IFn)const__2.getRawRoot()).invoke(pops);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = pops;
            pops = null;
            object = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), const__5.getRawRoot(), object8);
        } else {
            index$least_pop_slice$get_slice__15649 index$least_pop_slice$get_slice__15649 = get_slice;
            get_slice = null;
            Object object9 = ((IFn)const__6.getRawRoot()).invoke(offsets);
            Object object10 = offsets;
            offsets = null;
            object = ((IFn)index$least_pop_slice$get_slice__15649).invoke(object9, ((IFn)const__7.getRawRoot()).invoke(object10));
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return index$least_pop_slice.invokeStatic(object6, object7, object8, object9, object10);
    }
}


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
import datomic.external_sort$chunk_seq$fn__14404;

public final class external_sort$chunk_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"iterate");
    public static final Var const__5 = RT.var((String)"datomic.external-sort", (String)"next-chunk");

    public static Object invokeStatic(Object max_size, Object sizer, Object iter2) {
        external_sort$chunk_seq$fn__14404 external_sort$chunk_seq$fn__14404 = new external_sort$chunk_seq$fn__14404(sizer, max_size);
        Object object = max_size;
        max_size = null;
        Object object2 = sizer;
        sizer = null;
        Object object3 = iter2;
        iter2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke((Object)external_sort$chunk_seq$fn__14404, ((IFn)const__5.getRawRoot()).invoke(object, object2, object3))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return external_sort$chunk_seq.invokeStatic(object4, object5, object6);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.iter.MergeIter;

public final class iter$merge_iters
extends AFunction {
    public static final Object const__1 = 2L;
    public static final Var const__6 = RT.var((String)"datomic.iter", (String)"least-index");
    public static final Var const__7 = RT.var((String)"datomic.iter", (String)"merge-iters");

    public static Object invokeStatic(Object cmp, Object iter1, Object iter2, Object iter3, Object iter4, Object iter5) {
        Object object = cmp;
        Object object2 = iter1;
        iter1 = null;
        Object object3 = cmp;
        cmp = null;
        Object object4 = iter2;
        iter2 = null;
        Object object5 = iter3;
        iter3 = null;
        Object object6 = iter4;
        iter4 = null;
        Object object7 = iter5;
        iter5 = null;
        return ((IFn)const__7.getRawRoot()).invoke(object, object2, ((IFn)const__7.getRawRoot()).invoke(object3, object4, object5, object6, object7));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return iter$merge_iters.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    public static Object invokeStatic(Object cmp, Object iter1, Object iter2, Object iter3, Object iter4) {
        Object object = cmp;
        Object object2 = iter1;
        iter1 = null;
        Object object3 = cmp;
        cmp = null;
        Object object4 = iter2;
        iter2 = null;
        Object object5 = iter3;
        iter3 = null;
        Object object6 = iter4;
        iter4 = null;
        return ((IFn)const__7.getRawRoot()).invoke(object, object2, ((IFn)const__7.getRawRoot()).invoke(object3, object4, object5, object6));
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
        return iter$merge_iters.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object cmp, Object iter1, Object iter2, Object iter3) {
        Object object = cmp;
        Object object2 = iter1;
        iter1 = null;
        Object object3 = cmp;
        cmp = null;
        Object object4 = iter2;
        iter2 = null;
        Object object5 = iter3;
        iter3 = null;
        return ((IFn)const__7.getRawRoot()).invoke(object, object2, ((IFn)const__7.getRawRoot()).invoke(object3, object4, object5));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return iter$merge_iters.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object cmp, Object iter1, Object iter2) {
        Object object;
        Object object2;
        Object and__5236__auto__11792;
        Object object3 = and__5236__auto__11792 = iter1;
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = iter2;
        } else {
            object2 = and__5236__auto__11792;
            and__5236__auto__11792 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] arr = RT.object_array((Object)const__1);
            Object object4 = iter1;
            iter1 = null;
            RT.aset((Object[])arr, (int)RT.intCast((long)0L), (Object)object4);
            Object object5 = iter2;
            iter2 = null;
            RT.aset((Object[])arr, (int)RT.intCast((long)1L), (Object)object5);
            cmp = null;
            arr = null;
            object = new MergeIter(cmp, arr, ((IFn.OOL)const__6.getRawRoot()).invokePrim(cmp, (Object)arr));
        } else {
            Object or__5238__auto__11793;
            Object object6 = iter1;
            iter1 = null;
            Object object7 = or__5238__auto__11793 = object6;
            if (object7 != null && object7 != Boolean.FALSE) {
                object = or__5238__auto__11793;
                or__5238__auto__11793 = null;
            } else {
                object = iter2;
                Object var2_2 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return iter$merge_iters.invokeStatic(object4, object5, object6);
    }
}


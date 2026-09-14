/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$merge_seqs$fn__22313;
import datomic.integrity$merge_seqs$fn__22315;
import java.util.Comparator;

public final class integrity$merge_seqs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"datomic.integrity", (String)"merge-seqs");

    public static Object invokeStatic(Object cmp, Object s1, Object s2, Object s32, Object s4) {
        Object object = cmp;
        Object object2 = s1;
        s1 = null;
        Object object3 = cmp;
        cmp = null;
        Object object4 = s2;
        s2 = null;
        Object object5 = s32;
        s32 = null;
        Object object6 = s4;
        s4 = null;
        return ((IFn)const__4.getRawRoot()).invoke(object, object2, ((IFn)const__4.getRawRoot()).invoke(object3, object4, object5, object6));
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
        return integrity$merge_seqs.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object cmp, Object s1, Object s2, Object s32) {
        Object object = cmp;
        Object object2 = s1;
        s1 = null;
        Object object3 = cmp;
        cmp = null;
        Object object4 = s2;
        s2 = null;
        Object object5 = s32;
        s32 = null;
        return ((IFn)const__4.getRawRoot()).invoke(object, object2, ((IFn)const__4.getRawRoot()).invoke(object3, object4, object5));
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
        return integrity$merge_seqs.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object cmp, Object s1, Object s2) {
        Object object;
        Object object2;
        Object and__5236__auto__22318;
        Object object3 = s1;
        s1 = null;
        Object s12 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object object4 = s2;
        s2 = null;
        Object s22 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object object5 = and__5236__auto__22318 = s12;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = s22;
        } else {
            object2 = and__5236__auto__22318;
            and__5236__auto__22318 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object vec__22310;
            Object vec__22307;
            Object object6 = vec__22307 = s12;
            vec__22307 = null;
            Object seq__22308 = ((IFn)const__0.getRawRoot()).invoke(object6);
            Object first__22309 = ((IFn)const__1.getRawRoot()).invoke(seq__22308);
            Object object7 = seq__22308;
            seq__22308 = null;
            Object seq__223082 = ((IFn)const__2.getRawRoot()).invoke(object7);
            Object object8 = first__22309;
            first__22309 = null;
            Object o1 = object8;
            Object object9 = seq__223082;
            seq__223082 = null;
            Object m1 = object9;
            Object object10 = vec__22310 = s22;
            vec__22310 = null;
            Object seq__22311 = ((IFn)const__0.getRawRoot()).invoke(object10);
            Object first__22312 = ((IFn)const__1.getRawRoot()).invoke(seq__22311);
            Object object11 = seq__22311;
            seq__22311 = null;
            Object seq__223112 = ((IFn)const__2.getRawRoot()).invoke(object11);
            Object object12 = first__22312;
            first__22312 = null;
            Object o2 = object12;
            Object object13 = seq__223112;
            seq__223112 = null;
            Object m2 = object13;
            if ((long)((Comparator)cmp).compare(o1, o2) < 0L) {
                o1 = null;
                m1 = null;
                cmp = null;
                s22 = null;
                object = new LazySeq((IFn)new integrity$merge_seqs$fn__22313(o1, m1, cmp, s22));
            } else {
                s12 = null;
                o2 = null;
                cmp = null;
                m2 = null;
                object = new LazySeq((IFn)new integrity$merge_seqs$fn__22315(s12, o2, cmp, m2));
            }
        } else {
            Object or__5238__auto__22319;
            Object object14 = s12;
            s12 = null;
            Object object15 = or__5238__auto__22319 = object14;
            if (object15 != null && object15 != Boolean.FALSE) {
                object = or__5238__auto__22319;
                or__5238__auto__22319 = null;
            } else {
                object = s22;
                s22 = null;
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
        return integrity$merge_seqs.invokeStatic(object4, object5, object6);
    }
}


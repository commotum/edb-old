/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class domain$lookup_with_object_cache$fn__16813
extends AFunction {
    Object load_counter;
    public static final Var const__0 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__1 = RT.keyword(null, (String)"ocache");
    public static final Var const__2 = RT.var((String)"datomic.measure.io-stats", (String)"*io-index*");
    public static final Var const__3 = RT.var((String)"datomic.measure.io-stats", (String)"*io-seg-type*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__5 = RT.keyword(null, (String)"dir");
    public static final Var const__6 = RT.var((String)"datomic.measure.io-trace", (String)"note!");
    public static final Keyword const__8 = RT.keyword(null, (String)"miss");
    public static final Var const__9 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__10 = RT.keyword(null, (String)"DirLoads");
    public static final Object const__11 = 1L;
    public static final Keyword const__12 = RT.keyword(null, (String)"ObjectCache");
    public static final Object const__13 = 0L;
    public static final Keyword const__14 = RT.keyword(null, (String)"hit");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");

    public domain$lookup_with_object_cache$fn__16813(Object object) {
        this.load_counter = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object k, Object h_or_m) {
        Object object;
        IFn iFn;
        block15: {
            Object object2;
            Object and__5236__auto__16817;
            Object object3;
            Object and__5236__auto__16816;
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            Object index_counter = const__2.get();
            Object seg_type_counter = const__3.get();
            Object object4 = and__5236__auto__16816 = ((IFn)const__4.getRawRoot()).invoke(seg_type_counter, (Object)const__5);
            if (object4 != null && object4 != Boolean.FALSE) {
                object3 = index_counter;
            } else {
                object3 = and__5236__auto__16816;
                and__5236__auto__16816 = null;
            }
            Object leaf_QMARK_ = object3;
            Object object5 = k;
            k = null;
            ((IFn)const__6.getRawRoot()).invoke(object5, index_counter);
            Object object6 = leaf_QMARK_;
            if (object6 != null && object6 != Boolean.FALSE) {
                ((IFn)const__0.getRawRoot()).invoke(index_counter);
            }
            Object object7 = and__5236__auto__16817 = seg_type_counter;
            if (object7 != null && object7 != Boolean.FALSE) {
                object2 = ((IFn)const__4.getRawRoot()).invoke(seg_type_counter, (Object)const__5);
            } else {
                object2 = and__5236__auto__16817;
                and__5236__auto__16817 = null;
            }
            if (object2 != null && object2 != Boolean.FALSE) {
                ((IFn)const__0.getRawRoot()).invoke(seg_type_counter);
            }
            if (Util.equiv((Object)const__8, (Object)h_or_m)) {
                Object object8 = leaf_QMARK_;
                leaf_QMARK_ = null;
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = index_counter;
                    index_counter = null;
                    ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.load_counter).invoke(object9));
                } else {
                    Object object10 = seg_type_counter;
                    if (object10 != null && object10 != Boolean.FALSE) {
                        if (Util.equiv((Object)seg_type_counter, (Object)const__5)) {
                            ((IFn)const__9.getRawRoot()).invoke((Object)const__10, const__11);
                        }
                        Object object11 = seg_type_counter;
                        seg_type_counter = null;
                        ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.load_counter).invoke(object11));
                    }
                }
            }
            iFn = (IFn)const__9.getRawRoot();
            Object object12 = h_or_m;
            h_or_m = null;
            Object G__16814 = object12;
            switch (Util.hash((Object)G__16814) >> 1 & 1) {
                case 0: {
                    if (G__16814 != const__8) break;
                    object = const__13;
                    break block15;
                }
                case 1: {
                    if (G__16814 != const__14) break;
                    object = const__11;
                    break block15;
                }
            }
            Object object13 = G__16814;
            G__16814 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__15.getRawRoot()).invoke((Object)"No matching clause: ", object13));
        }
        domain$lookup_with_object_cache$fn__16813 this_ = null;
        return iFn.invoke((Object)const__12, object);
    }
}


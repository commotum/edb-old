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

public final class domain$peer_object_lookup$fn__16795
extends AFunction {
    Object load_counter;
    public static final Var const__0 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__1 = RT.keyword(null, (String)"ocache");
    public static final Var const__2 = RT.var((String)"datomic.measure.io-stats", (String)"*io-index*");
    public static final Var const__3 = RT.var((String)"datomic.measure.io-stats", (String)"*io-seg-type*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__5 = RT.keyword(null, (String)"dir");
    public static final Keyword const__7 = RT.keyword(null, (String)"miss");
    public static final Var const__8 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__9 = RT.keyword(null, (String)"DirLoads");
    public static final Object const__10 = 1L;
    public static final Keyword const__11 = RT.keyword(null, (String)"ObjectCache");
    public static final Object const__12 = 0L;
    public static final Keyword const__13 = RT.keyword(null, (String)"hit");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"str");

    public domain$peer_object_lookup$fn__16795(Object object) {
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
            Object and__5236__auto__16799;
            Object leaf_QMARK_;
            Object object3;
            Object and__5236__auto__16798;
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            Object index_counter = const__2.get();
            Object seg_type_counter = const__3.get();
            Object object4 = and__5236__auto__16798 = ((IFn)const__4.getRawRoot()).invoke(seg_type_counter, (Object)const__5);
            if (object4 != null && object4 != Boolean.FALSE) {
                object3 = index_counter;
            } else {
                object3 = and__5236__auto__16798;
                and__5236__auto__16798 = null;
            }
            Object object5 = leaf_QMARK_ = object3;
            if (object5 != null && object5 != Boolean.FALSE) {
                ((IFn)const__0.getRawRoot()).invoke(index_counter);
            }
            Object object6 = and__5236__auto__16799 = seg_type_counter;
            if (object6 != null && object6 != Boolean.FALSE) {
                object2 = ((IFn)const__4.getRawRoot()).invoke(seg_type_counter, (Object)const__5);
            } else {
                object2 = and__5236__auto__16799;
                and__5236__auto__16799 = null;
            }
            if (object2 != null && object2 != Boolean.FALSE) {
                ((IFn)const__0.getRawRoot()).invoke(seg_type_counter);
            }
            if (Util.equiv((Object)const__7, (Object)h_or_m)) {
                Object object7 = leaf_QMARK_;
                leaf_QMARK_ = null;
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = index_counter;
                    index_counter = null;
                    ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.load_counter).invoke(object8));
                } else {
                    Object object9 = seg_type_counter;
                    if (object9 != null && object9 != Boolean.FALSE) {
                        if (Util.equiv((Object)seg_type_counter, (Object)const__5)) {
                            ((IFn)const__8.getRawRoot()).invoke((Object)const__9, const__10);
                        }
                        Object object10 = seg_type_counter;
                        seg_type_counter = null;
                        ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.load_counter).invoke(object10));
                    }
                }
            }
            iFn = (IFn)const__8.getRawRoot();
            Object object11 = h_or_m;
            h_or_m = null;
            Object G__16796 = object11;
            switch (Util.hash((Object)G__16796) >> 1 & 1) {
                case 0: {
                    if (G__16796 != const__7) break;
                    object = const__12;
                    break block15;
                }
                case 1: {
                    if (G__16796 != const__13) break;
                    object = const__10;
                    break block15;
                }
            }
            Object object12 = G__16796;
            G__16796 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__14.getRawRoot()).invoke((Object)"No matching clause: ", object12));
        }
        domain$peer_object_lookup$fn__16795 this_ = null;
        return iFn.invoke((Object)const__11, object);
    }
}


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
import datomic.cluster$write_vals_STAR_$fn__10660;
import datomic.cluster$write_vals_STAR_$fn__10666;

public final class cluster$write_vals_STAR_
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"index");
    public static final Keyword const__1 = RT.keyword((String)"index", (String)"write-val");
    public static final Keyword const__2 = RT.keyword(null, (String)"clusterfs");
    public static final Keyword const__3 = RT.keyword((String)"clusterfs", (String)"write-val");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"IndexWriteNsec");
    public static final Keyword const__6 = RT.keyword(null, (String)"FulltextWriteNsec");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__8 = RT.var((String)"datomic.cluster", (String)"segment-pacing-msec");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cs, Object source, Object vmap) {
        Keyword keyword;
        Keyword event;
        block9: {
            Keyword keyword2;
            block8: {
                Object G__10657 = source;
                switch (Util.hash((Object)G__10657) >> 1 & 1) {
                    case 0: {
                        if (G__10657 != const__0) break;
                        keyword2 = const__1;
                        break block8;
                    }
                    case 1: {
                        if (G__10657 != const__2) break;
                        keyword2 = const__3;
                        break block8;
                    }
                }
                Object object = G__10657;
                G__10657 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"No matching clause: ", object));
            }
            event = keyword2;
            Object object = source;
            source = null;
            Object G__10658 = object;
            switch (Util.hash((Object)G__10658) >> 1 & 1) {
                case 0: {
                    if (G__10658 != const__0) break;
                    keyword = const__5;
                    break block9;
                }
                case 1: {
                    if (G__10658 != const__2) break;
                    keyword = const__6;
                    break block9;
                }
            }
            Object object2 = G__10658;
            G__10658 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"No matching clause: ", object2));
        }
        Keyword metric = keyword;
        Object pacing = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot());
        Object object = cs;
        cs = null;
        Object object3 = pacing;
        pacing = null;
        Object object4 = vmap;
        vmap = null;
        Object rets = ((IFn)const__9.getRawRoot()).invoke((Object)new cluster$write_vals_STAR_$fn__10660(object, object3), object4);
        Keyword keyword3 = metric;
        metric = null;
        Keyword keyword4 = event;
        event = null;
        return ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)new cluster$write_vals_STAR_$fn__10666(keyword3, keyword4), rets));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$write_vals_STAR_.invokeStatic(object4, object5, object6);
    }
}


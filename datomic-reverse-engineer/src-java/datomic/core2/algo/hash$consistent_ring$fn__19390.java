/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.TreeMap;

public final class hash$consistent_ring$fn__19390
extends AFunction {
    Object key_hashes_fn;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public hash$consistent_ring$fn__19390(Object object) {
        this.key_hashes_fn = object;
    }

    public Object invoke(Object tm, Object p__19389) {
        Object weight;
        Object object = p__19389;
        p__19389 = null;
        Object vec__19391 = object;
        Object name = RT.nth((Object)vec__19391, (int)RT.intCast((long)0L), null);
        Object object2 = vec__19391;
        vec__19391 = null;
        Object object3 = weight = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        weight = null;
        Object seq_19394 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3, ((IFn)this.key_hashes_fn).invoke(name)));
        Object chunk_19395 = null;
        long count_19396 = 0L;
        long i_19397 = 0L;
        while (true) {
            Object h;
            Object temp__5804__auto__19400;
            if (i_19397 < count_19396) {
                Object h3;
                Object object4 = h3 = ((Indexed)chunk_19395).nth(RT.intCast((long)i_19397));
                h3 = null;
                ((TreeMap)tm).put(object4, name);
                Object object5 = seq_19394;
                seq_19394 = null;
                Object object6 = chunk_19395;
                chunk_19395 = null;
                ++i_19397;
                chunk_19395 = object6;
                seq_19394 = object5;
                continue;
            }
            Object object7 = seq_19394;
            seq_19394 = null;
            Object object8 = temp__5804__auto__19400 = ((IFn)const__3.getRawRoot()).invoke(object7);
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object object9 = temp__5804__auto__19400;
            temp__5804__auto__19400 = null;
            Object seq_193942 = object9;
            Object object10 = ((IFn)const__7.getRawRoot()).invoke(seq_193942);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object c__6065__auto__19399 = ((IFn)const__8.getRawRoot()).invoke(seq_193942);
                Object object11 = seq_193942;
                seq_193942 = null;
                Object object12 = c__6065__auto__19399;
                Object object13 = c__6065__auto__19399;
                c__6065__auto__19399 = null;
                i_19397 = RT.intCast((long)0L);
                count_19396 = RT.intCast((int)RT.count((Object)object13));
                chunk_19395 = object12;
                seq_19394 = ((IFn)const__9.getRawRoot()).invoke(object11);
                continue;
            }
            Object object14 = h = ((IFn)const__12.getRawRoot()).invoke(seq_193942);
            h = null;
            ((TreeMap)tm).put(object14, name);
            Object object15 = seq_193942;
            seq_193942 = null;
            i_19397 = 0L;
            count_19396 = 0L;
            chunk_19395 = null;
            seq_19394 = ((IFn)const__13.getRawRoot()).invoke(object15);
        }
        Object var1_1 = null;
        return tm;
    }
}


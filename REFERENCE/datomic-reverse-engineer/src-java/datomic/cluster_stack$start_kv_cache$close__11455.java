/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class cluster_stack$start_kv_cache$close__11455
extends AFunction {
    Object valcache;
    Object memcached;
    Object local_memcached;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"next");

    public cluster_stack$start_kv_cache$close__11455(Object object, Object object2, Object object3) {
        this.valcache = object;
        this.memcached = object2;
        this.local_memcached = object3;
    }

    public Object invoke() {
        Object seq_11456 = ((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)this.memcached, (Object)this.valcache, (Object)this.local_memcached));
        Object chunk_11457 = null;
        long count_11458 = 0L;
        long i_11459 = 0L;
        while (true) {
            Object cache2;
            Object temp__5457__auto__11462;
            if (i_11459 < count_11458) {
                Object cache3;
                Object object = cache3 = ((Indexed)chunk_11457).nth(RT.intCast((long)i_11459));
                if (object != null && object != Boolean.FALSE) {
                    Object object2 = cache3;
                    cache3 = null;
                    ((AutoCloseable)object2).close();
                }
                Object object3 = seq_11456;
                seq_11456 = null;
                Object object4 = chunk_11457;
                chunk_11457 = null;
                ++i_11459;
                chunk_11457 = object4;
                seq_11456 = object3;
                continue;
            }
            Object object = seq_11456;
            seq_11456 = null;
            Object object5 = temp__5457__auto__11462 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object5 == null || object5 == Boolean.FALSE) break;
            Object object6 = temp__5457__auto__11462;
            temp__5457__auto__11462 = null;
            Object seq_114562 = object6;
            Object object7 = ((IFn)const__4.getRawRoot()).invoke(seq_114562);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object c__5719__auto__11461 = ((IFn)const__5.getRawRoot()).invoke(seq_114562);
                Object object8 = seq_114562;
                seq_114562 = null;
                Object object9 = c__5719__auto__11461;
                Object object10 = c__5719__auto__11461;
                c__5719__auto__11461 = null;
                i_11459 = RT.intCast((long)0L);
                count_11458 = RT.intCast((int)RT.count((Object)object10));
                chunk_11457 = object9;
                seq_11456 = ((IFn)const__6.getRawRoot()).invoke(object8);
                continue;
            }
            Object object11 = cache2 = ((IFn)const__9.getRawRoot()).invoke(seq_114562);
            if (object11 != null && object11 != Boolean.FALSE) {
                Object object12 = cache2;
                cache2 = null;
                ((AutoCloseable)object12).close();
            }
            Object object13 = seq_114562;
            seq_114562 = null;
            i_11459 = 0L;
            count_11458 = 0L;
            chunk_11457 = null;
            seq_11456 = ((IFn)const__10.getRawRoot()).invoke(object13);
        }
        return null;
    }
}


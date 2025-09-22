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
package datomic.datalog;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;

public final class FnRel$fn__18356
extends AFunction {
    Object join_map;
    Object bindings;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public FnRel$fn__18356(Object object, Object object2) {
        this.join_map = object;
        this.bindings = object2;
    }

    public Object invoke() {
        Object seq_18352 = ((IFn)const__0.getRawRoot()).invoke(this.join_map);
        Object chunk_18353 = null;
        long count_18354 = 0L;
        long i_18355 = 0L;
        while (true) {
            Object temp__5457__auto__18365;
            if (i_18355 < count_18354) {
                Object vec__18357 = ((Indexed)chunk_18353).nth(RT.uncheckedIntCast((long)i_18355));
                Object k = RT.nth((Object)vec__18357, (int)RT.uncheckedIntCast((long)0L), null);
                Object object = vec__18357;
                vec__18357 = null;
                Object v = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
                Object object2 = k;
                k = null;
                Object object3 = v;
                v = null;
                RT.aset((Object[])((Object[])this.bindings), (int)RT.uncheckedIntCast((Object)object2), (Object)object3);
                Object object4 = seq_18352;
                seq_18352 = null;
                Object object5 = chunk_18353;
                chunk_18353 = null;
                ++i_18355;
                chunk_18353 = object5;
                seq_18352 = object4;
                continue;
            }
            Object object = seq_18352;
            seq_18352 = null;
            Object object6 = temp__5457__auto__18365 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__18365;
            temp__5457__auto__18365 = null;
            Object seq_183522 = object7;
            Object object8 = ((IFn)const__8.getRawRoot()).invoke(seq_183522);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__18364 = ((IFn)const__9.getRawRoot()).invoke(seq_183522);
                Object object9 = seq_183522;
                seq_183522 = null;
                Object object10 = c__5719__auto__18364;
                Object object11 = c__5719__auto__18364;
                c__5719__auto__18364 = null;
                i_18355 = (int)0L;
                count_18354 = RT.count((Object)object11);
                chunk_18353 = object10;
                seq_18352 = ((IFn)const__10.getRawRoot()).invoke(object9);
                continue;
            }
            Object vec__18360 = ((IFn)const__12.getRawRoot()).invoke(seq_183522);
            Object k = RT.nth((Object)vec__18360, (int)RT.uncheckedIntCast((long)0L), null);
            Object object12 = vec__18360;
            vec__18360 = null;
            Object v = RT.nth((Object)object12, (int)RT.uncheckedIntCast((long)1L), null);
            Object object13 = k;
            k = null;
            Object object14 = v;
            v = null;
            RT.aset((Object[])((Object[])this.bindings), (int)RT.uncheckedIntCast((Object)object13), (Object)object14);
            Object object15 = seq_183522;
            seq_183522 = null;
            i_18355 = 0L;
            count_18354 = 0L;
            chunk_18353 = null;
            seq_18352 = ((IFn)const__13.getRawRoot()).invoke(object15);
        }
        return null;
    }
}


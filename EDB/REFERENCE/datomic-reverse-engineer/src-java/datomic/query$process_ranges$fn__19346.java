/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class query$process_ranges$fn__19346
extends AFunction {
    Object assoc_stronger;
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"="), Symbol.intern(null, (String)">"), Symbol.intern(null, (String)">=")});
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"range-starts"));
    public static final AFn const__8 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"="), Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)"<=")});
    public static final AFn const__10 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"range-whiles"));

    public query$process_ranges$fn__19346(Object object) {
        this.assoc_stronger = object;
    }

    public Object invoke(Object m, Object p__19345) {
        Object object;
        Object object2;
        Object object3 = p__19345;
        p__19345 = null;
        Object vec__19347 = object3;
        Object cmp = RT.nth((Object)vec__19347, (int)RT.intCast((long)0L), null);
        Object var = RT.nth((Object)vec__19347, (int)RT.intCast((long)1L), null);
        Object object4 = vec__19347;
        vec__19347 = null;
        Object object5 = RT.nth((Object)object4, (int)RT.intCast((long)2L), null);
        Object object6 = m;
        m = null;
        Object G__19350 = object6;
        Object object7 = ((IFn)const__4).invoke(cmp);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = G__19350;
            G__19350 = null;
            object2 = ((IFn)const__5.getRawRoot()).invoke(object8, (Object)const__7, this_.assoc_stronger, var, object5, cmp);
        } else {
            object2 = G__19350;
            G__19350 = null;
        }
        Object G__193502 = object2;
        Object object9 = ((IFn)const__8).invoke(cmp);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = G__193502;
            G__193502 = null;
            Object object11 = var;
            var = null;
            Object object12 = object5;
            object5 = null;
            IPersistentVector iPersistentVector = Tuple.create((Object)cmp, (Object)object12);
            Object object13 = cmp;
            cmp = null;
            query$process_ranges$fn__19346 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object10, (Object)const__10, this_.assoc_stronger, object11, (Object)iPersistentVector, object13);
        } else {
            object = G__193502;
            G__193502 = null;
        }
        return object;
    }
}


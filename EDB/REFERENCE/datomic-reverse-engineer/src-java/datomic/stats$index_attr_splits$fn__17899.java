/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class stats$index_attr_splits$fn__17899
extends AFunction {
    long split_n;
    public static final Object const__2 = 1L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public stats$index_attr_splits$fn__17899(long l) {
        this.split_n = l;
    }

    public Object invoke(Object p__17898, Object datom) {
        IPersistentVector iPersistentVector;
        Object object = p__17898;
        p__17898 = null;
        Object vec__17900 = object;
        Object ret = RT.nth((Object)vec__17900, (int)RT.intCast((long)0L), null);
        Object nexte = RT.nth((Object)vec__17900, (int)RT.intCast((long)1L), null);
        Object object2 = vec__17900;
        vec__17900 = null;
        Object c = RT.nth((Object)object2, (int)RT.intCast((long)2L), null);
        if (Util.equiv((long)this.split_n, (Object)c)) {
            Object object3 = ret;
            ret = null;
            Object object4 = nexte;
            nexte = null;
            Object object5 = ((IFn)const__5.getRawRoot()).invoke(object3, (Object)Tuple.create((Object)object4, (Object)Numbers.num((long)this.split_n)));
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = datom;
            datom = null;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            iPersistentVector = Tuple.create((Object)object5, (Object)object7, (Object)const__2);
        } else {
            Object object8 = ret;
            ret = null;
            Object object9 = nexte;
            nexte = null;
            Object object10 = c;
            c = null;
            iPersistentVector = Tuple.create((Object)object8, (Object)object9, (Object)Numbers.inc((Object)object10));
        }
        return iPersistentVector;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.domain;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class DeserializingRepairingLookup$fn__16804
extends AFunction {
    Object k;
    Object v;
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"deserialize");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"report-val-fn-fail");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public DeserializingRepairingLookup$fn__16804(Object object, Object object2) {
        this.k = object;
        this.v = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.v);
        }
        catch (Throwable t2) {
            IFn iFn = (IFn)const__1.getRawRoot();
            Object t2 = null;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = this.v;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            object = iFn.invoke((Object)t2, object3, this.k);
        }
        return object;
    }
}


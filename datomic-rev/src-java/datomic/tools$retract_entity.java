/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class tools$retract_entity
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"db.fn", (String)"retractEntity");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object d) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = d;
        d = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return Tuple.create((Object)const__0, (Object)object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$retract_entity.invokeStatic(object2);
    }
}


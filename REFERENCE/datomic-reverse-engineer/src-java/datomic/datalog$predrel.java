/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import datomic.datalog.PredRel;

public final class datalog$predrel
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fn"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"needs-source"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object emap2, Object consts) {
        Object object = db2;
        db2 = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = emap2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = emap2;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Integer n = RT.count((Object)object5);
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object6 = emap2;
        emap2 = null;
        Object object7 = iLookupThunk3.get(object6);
        if (iLookupThunk3 == object7) {
            __thunk__2__ = __site__2__.fault(object6);
            object7 = __thunk__2__.get(object6);
        }
        Object object8 = consts;
        consts = null;
        return new PredRel(object, object3, n, object7, object8);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$predrel.invokeStatic(object4, object5, object6);
    }
}


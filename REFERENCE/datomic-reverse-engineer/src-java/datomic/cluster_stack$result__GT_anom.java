/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;

public final class cluster_stack$result__GT_anom
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"fault");
    public static final Keyword const__2 = RT.keyword((String)"datomic.cluster-stack", (String)"cluster-result");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"category"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object result2) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = result2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            object = result2;
            result2 = null;
        } else {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__0;
            objectArray[1] = const__1;
            objectArray[2] = const__2;
            Object object4 = result2;
            result2 = null;
            objectArray[3] = object4;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster_stack$result__GT_anom.invokeStatic(object2);
    }
}


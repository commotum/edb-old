/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import java.nio.Buffer;

public final class spi$no_val_error
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"fault");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Keyword const__4 = RT.keyword((String)"datomic.core2.val-store.spi", (String)"key");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object k, Object v) {
        IPersistentMap iPersistentMap;
        Object object;
        Object and__5579__auto__21850;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = v;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = and__5579__auto__21850 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = v;
            v = null;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            object = ((Buffer)object6).hasRemaining() ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5579__auto__21850;
            Object var2_2 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            iPersistentMap = null;
        } else {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__1;
            objectArray[1] = const__2;
            objectArray[2] = const__3;
            objectArray[3] = "No value specified";
            objectArray[4] = const__4;
            Object object7 = k;
            k = null;
            objectArray[5] = object7;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return spi$no_val_error.invokeStatic(object3, object4);
    }
}


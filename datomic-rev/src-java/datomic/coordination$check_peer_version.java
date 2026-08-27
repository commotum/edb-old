/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class coordination$check_peer_version
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"read-transactor-location-failed");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"peer-transactor-mismatch");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"datomic.config", (String)"property");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"peer-version"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"peer-version"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"version"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object endpoint) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = endpoint;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
        } else {
            ((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)"Could not read transactor location from storage", endpoint);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = endpoint;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        if (Util.equiv((long)2L, (Object)object5)) {
            object = null;
        } else {
            IFn iFn = (IFn)const__1.getRawRoot();
            IFn iFn2 = (IFn)const__6.getRawRoot();
            Object object6 = ((IFn)const__7.getRawRoot()).invoke((Object)"datomic.version");
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object7 = endpoint;
            endpoint = null;
            Object object8 = iLookupThunk3.get(object7);
            if (iLookupThunk3 == object8) {
                __thunk__2__ = __site__2__.fault(object7);
                object8 = __thunk__2__.get(object7);
            }
            object = iFn.invoke((Object)const__5, iFn2.invoke((Object)"Peer version ", object6, (Object)" is not compatible with transactor version ", object8));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$check_peer_version.invokeStatic(object2);
    }
}


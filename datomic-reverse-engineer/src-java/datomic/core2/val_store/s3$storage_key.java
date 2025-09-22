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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class s3$storage_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__2 = RT.keyword(null, (String)"skip");
    public static final Var const__4 = RT.var((String)"datomic.core2.val-store.spi", (String)"splice-partition-key");
    public static final Var const__5 = RT.var((String)"datomic.core2.val-store.spi", (String)"partition-key");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic.core2.val-store.opts", (String)"partition"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object prefix, Object key, Object opts) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = prefix;
        prefix = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = opts;
        opts = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (Util.equiv((Object)const__2, (Object)object4)) {
            object = key;
            key = null;
        } else {
            Object object5 = key;
            Object object6 = key;
            key = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object5, ((IFn)const__5.getRawRoot()).invoke(object6));
        }
        return iFn.invoke(object2, (Object)"/", object);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$storage_key.invokeStatic(object4, object5, object6);
    }
}


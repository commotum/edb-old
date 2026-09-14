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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class coordination$fn__11677
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"unsupported-protocol");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = cluster_conf;
        cluster_conf = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return iFn.invoke((Object)const__1, iFn2.invoke((Object)"Unsupported protocol ", object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$fn__11677.invokeStatic(object2);
    }
}


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

public final class s3_kv$storage_from_conf_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3-kv", (String)"s3-storage");
    public static final Keyword const__1 = RT.keyword(null, (String)"s3");
    public static final Var const__2 = RT.var((String)"datomic.s3", (String)"s3-service");
    public static final Keyword const__3 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__5 = RT.keyword(null, (String)"base");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"system-root"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-s3-path"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object conf) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = ((IFn)const__2.getRawRoot()).invoke(conf);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = conf;
        conf = null;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        return iFn.invoke((Object)const__1, object, (Object)const__3, object3, (Object)const__5, object5);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_kv$storage_from_conf_.invokeStatic(object2);
    }
}


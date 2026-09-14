/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class ddb_s3_cluster$get_valid_config_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__2 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-config-refval");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"every?");
    public static final AFn const__8 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"s3-vals-bucket"), (Object)RT.keyword(null, (String)"s3-vals-prefix"), (Object)RT.keyword(null, (String)"fs-vals-path"));
    public static final Var const__9 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__10 = RT.keyword((String)"ddb-s3-cluster", (String)"missing-configuration");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-dynamodb-table"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object kv_store2, Object cluster_conf) {
        Object object;
        Object object2;
        Object and__5236__auto__22775;
        Object object3 = kv_store2;
        kv_store2 = null;
        Object cluster2 = ((IFn)const__0.getRawRoot()).invoke(object3, cluster_conf);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = cluster2;
        cluster2 = null;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        Object conf = object6;
        Object object7 = and__5236__auto__22775 = const__3.getRawRoot();
        if (object7 != null && object7 != Boolean.FALSE) {
            Object and__5236__auto__22774;
            Object object8 = and__5236__auto__22774 = conf;
            if (object8 != null && object8 != Boolean.FALSE) {
                object2 = ((IFn)const__4.getRawRoot()).invoke(conf, (Object)const__8);
            } else {
                object2 = and__5236__auto__22774;
                and__5236__auto__22774 = null;
            }
        } else {
            object2 = and__5236__auto__22775;
            and__5236__auto__22775 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = conf;
            conf = null;
        } else {
            IFn iFn = (IFn)const__9.getRawRoot();
            IFn iFn2 = (IFn)const__11.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = cluster_conf;
            cluster_conf = null;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            object = iFn.invoke((Object)const__10, iFn2.invoke(object10, (Object)" is not configured for ddb+s3 storage"));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb_s3_cluster$get_valid_config_BANG_.invokeStatic(object3, object4);
    }
}


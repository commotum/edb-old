/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class fulltext$promote_to_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cstore");
    public static final Keyword const__4 = RT.keyword(null, (String)"path");
    public static final Keyword const__5 = RT.keyword(null, (String)"baseid");
    public static final Keyword const__6 = RT.keyword(null, (String)"basefs");
    public static final Keyword const__7 = RT.keyword(null, (String)"delete-requests");
    public static final Var const__8 = RT.var((String)"datomic.fulltext", (String)"index-files");
    public static final Var const__9 = RT.var((String)"datomic.clusterfs", (String)"create-fs");
    public static final Keyword const__10 = RT.keyword(null, (String)"chunk-size");
    public static final Object const__11 = 54000L;
    public static final Keyword const__12 = RT.keyword(null, (String)"base");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__17 = RT.var((String)"datomic.clusterfs", (String)"chunk-keys");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"chunk-size"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"dir"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object indexing_job) {
        IPersistentVector iPersistentVector;
        Object filemap;
        Object object;
        Object object2 = indexing_job;
        indexing_job = null;
        Object map__14544 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__14544);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__14544;
            map__14544 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__14544;
            map__14544 = null;
        }
        Object map__145442 = object;
        Object cstore = RT.get((Object)map__145442, (Object)const__3);
        Object path2 = RT.get((Object)map__145442, (Object)const__4);
        Object baseid = RT.get((Object)map__145442, (Object)const__5);
        Object basefs = RT.get((Object)map__145442, (Object)const__6);
        Object object5 = map__145442;
        map__145442 = null;
        Object delete_requests = RT.get((Object)object5, (Object)const__7);
        Object object6 = filemap = ((IFn)const__8.getRawRoot()).invoke(path2);
        filemap = null;
        Object object7 = ((IFn)const__1.getRawRoot()).invoke(object6);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8;
            Object object9;
            IFn iFn = (IFn)const__9.getRawRoot();
            Object object10 = cstore;
            cstore = null;
            Object object11 = path2;
            path2 = null;
            Object object12 = ((IFn)const__8.getRawRoot()).invoke(object11);
            Object object13 = basefs;
            if (object13 != null && object13 != Boolean.FALSE) {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object14 = basefs;
                object9 = iLookupThunk.get(object14);
                if (iLookupThunk == object9) {
                    __thunk__0__ = __site__0__.fault(object14);
                    object9 = __thunk__0__.get(object14);
                }
            } else {
                object9 = const__11;
            }
            Object object15 = basefs;
            if (object15 != null && object15 != Boolean.FALSE) {
                IFn iFn2 = (IFn)const__13.getRawRoot();
                Object object16 = const__14.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__1__;
                Object object17 = basefs;
                Object object18 = iLookupThunk.get(object17);
                if (iLookupThunk == object18) {
                    __thunk__1__ = __site__1__.fault(object17);
                    object18 = __thunk__1__.get(object17);
                }
                object8 = iFn2.invoke(object16, object18, ((IFn)const__16.getRawRoot()).invoke(delete_requests));
            } else {
                object8 = null;
            }
            Object object19 = basefs;
            basefs = null;
            Object object20 = delete_requests;
            delete_requests = null;
            iPersistentVector = Tuple.create((Object)iFn.invoke(object10, object12, (Object)const__10, object9, (Object)const__12, object8), (Object)((IFn)const__17.getRawRoot()).invoke(object19, ((IFn)const__16.getRawRoot()).invoke(object20)));
        } else {
            Object object21 = baseid;
            baseid = null;
            iPersistentVector = Tuple.create((Object)object21, null);
        }
        return iPersistentVector;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fulltext$promote_to_cluster.invokeStatic(object2);
    }
}


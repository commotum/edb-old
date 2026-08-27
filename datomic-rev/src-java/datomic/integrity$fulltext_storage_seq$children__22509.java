/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.clusterfs.ClusterFS;
import datomic.fulltext.Root;
import datomic.integrity$fulltext_storage_seq$children__22509$fn__22511;
import datomic.integrity$fulltext_storage_seq$children__22509$fn__22513;

public final class integrity$fulltext_storage_seq$children__22509
extends AFunction {
    Object olookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__10 = RT.var((String)"datomic.clusterfs", (String)"all-keys");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"attrmap"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public integrity$fulltext_storage_seq$children__22509(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object p__22508) {
        Object object;
        integrity$fulltext_storage_seq$children__22509 this_;
        Object map__22510;
        Object object2;
        Object object3 = p__22508;
        p__22508 = null;
        Object map__225102 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__225102);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__225102;
            map__225102 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__225102;
            map__225102 = null;
        }
        Object object6 = map__22510 = object2;
        map__22510 = null;
        Object seg = RT.get((Object)object6, (Object)const__3);
        if (seg instanceof Root) {
            IFn iFn = (IFn)const__6.getRawRoot();
            integrity$fulltext_storage_seq$children__22509$fn__22511 integrity$fulltext_storage_seq$children__22509$fn__22511 = new integrity$fulltext_storage_seq$children__22509$fn__22511(this_.olookup);
            IFn iFn2 = (IFn)const__7.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object7 = seg;
            seg = null;
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            this_ = null;
            object = iFn.invoke((Object)integrity$fulltext_storage_seq$children__22509$fn__22511, iFn2.invoke(object8));
        } else if (seg instanceof ClusterFS) {
            Object object9 = seg;
            seg = null;
            this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)new integrity$fulltext_storage_seq$children__22509$fn__22513(this_.olookup), ((IFn)const__10.getRawRoot()).invoke(object9));
        } else {
            object = null;
        }
        return object;
    }
}


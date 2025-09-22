/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
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
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$avet_dquark_seq$fn__22361;
import datomic.integrity$avet_dquark_seq$fn__22366;
import datomic.integrity$avet_dquark_seq$reify__22363;

public final class integrity$avet_dquark_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"datomic.integrity", (String)"merge-seqs");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 695, RT.keyword(null, (String)"column"), 9});
    public static final Keyword const__8 = RT.keyword(null, (String)"memidx");
    public static final Var const__9 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Keyword const__11 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__12 = RT.keyword(null, (String)"index");
    public static final Keyword const__13 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__14 = RT.keyword(null, (String)"history");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"indexing"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__9__ = __site__9__;

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object temp__5457__auto__22379;
        Object object2;
        Object temp__5457__auto__22378;
        Object object3;
        Object temp__5457__auto__22377;
        Object object4;
        Object index2;
        Object temp__5457__auto__22376;
        Object object5;
        IFn iFn = (IFn)const__0.getRawRoot();
        integrity$avet_dquark_seq$fn__22361 integrity$avet_dquark_seq$fn__22361 = new integrity$avet_dquark_seq$fn__22361();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        Object object6 = const__2.getRawRoot();
        IObj iObj = ((IObj)new integrity$avet_dquark_seq$reify__22363(null)).withMeta((IPersistentMap)const__7);
        IFn iFn3 = (IFn)const__0.getRawRoot();
        integrity$avet_dquark_seq$fn__22366 integrity$avet_dquark_seq$fn__22366 = new integrity$avet_dquark_seq$fn__22366(db2);
        IFn iFn4 = (IFn)const__9.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object7 = db2;
        Object object8 = iLookupThunk2.get(object7);
        if (iLookupThunk2 == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (iLookupThunk == (object5 = iLookupThunk.get(object8))) {
            __thunk__1__ = __site__1__.fault(object8);
            object5 = __thunk__1__.get(object8);
        }
        IPersistentVector iPersistentVector = Tuple.create((Object)const__8, (Object)iFn4.invoke(object5, d));
        IFn iFn5 = (IFn)const__9.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object9 = db2;
        Object object10 = iLookupThunk3.get(object9);
        if (iLookupThunk3 == object10) {
            __thunk__2__ = __site__2__.fault(object9);
            object10 = __thunk__2__.get(object9);
        }
        Object object11 = temp__5457__auto__22376 = object10;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = temp__5457__auto__22376;
            temp__5457__auto__22376 = null;
            index2 = object12;
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object13 = index2;
            index2 = null;
            object4 = iLookupThunk4.get(object13);
            if (iLookupThunk4 == object4) {
                __thunk__3__ = __site__3__.fault(object13);
                object4 = __thunk__3__.get(object13);
            }
        } else {
            object4 = null;
        }
        IPersistentVector iPersistentVector2 = Tuple.create((Object)const__11, (Object)iFn5.invoke(object4, d));
        IFn iFn6 = (IFn)const__9.getRawRoot();
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object14 = db2;
        Object object15 = iLookupThunk5.get(object14);
        if (iLookupThunk5 == object15) {
            __thunk__4__ = __site__4__.fault(object14);
            object15 = __thunk__4__.get(object14);
        }
        Object object16 = temp__5457__auto__22377 = object15;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = temp__5457__auto__22377;
            temp__5457__auto__22377 = null;
            index2 = object17;
            ILookupThunk iLookupThunk6 = __thunk__5__;
            Object object18 = index2;
            index2 = null;
            object3 = iLookupThunk6.get(object18);
            if (iLookupThunk6 == object3) {
                __thunk__5__ = __site__5__.fault(object18);
                object3 = __thunk__5__.get(object18);
            }
        } else {
            object3 = null;
        }
        IPersistentVector iPersistentVector3 = Tuple.create((Object)const__12, (Object)iFn6.invoke(object3, d));
        IFn iFn7 = (IFn)const__9.getRawRoot();
        ILookupThunk iLookupThunk7 = __thunk__6__;
        Object object19 = db2;
        Object object20 = iLookupThunk7.get(object19);
        if (iLookupThunk7 == object20) {
            __thunk__6__ = __site__6__.fault(object19);
            object20 = __thunk__6__.get(object19);
        }
        Object object21 = temp__5457__auto__22378 = object20;
        if (object21 != null && object21 != Boolean.FALSE) {
            Object object22 = temp__5457__auto__22378;
            temp__5457__auto__22378 = null;
            index2 = object22;
            ILookupThunk iLookupThunk8 = __thunk__7__;
            Object object23 = index2;
            index2 = null;
            object2 = iLookupThunk8.get(object23);
            if (iLookupThunk8 == object2) {
                __thunk__7__ = __site__7__.fault(object23);
                object2 = __thunk__7__.get(object23);
            }
        } else {
            object2 = null;
        }
        IPersistentVector iPersistentVector4 = Tuple.create((Object)const__13, (Object)iFn7.invoke(object2, d));
        IFn iFn8 = (IFn)const__9.getRawRoot();
        ILookupThunk iLookupThunk9 = __thunk__8__;
        Object object24 = db2;
        db2 = null;
        Object object25 = iLookupThunk9.get(object24);
        if (iLookupThunk9 == object25) {
            __thunk__8__ = __site__8__.fault(object24);
            object25 = __thunk__8__.get(object24);
        }
        Object object26 = temp__5457__auto__22379 = object25;
        if (object26 != null && object26 != Boolean.FALSE) {
            Object object27 = temp__5457__auto__22379;
            temp__5457__auto__22379 = null;
            index2 = object27;
            ILookupThunk iLookupThunk10 = __thunk__9__;
            Object object28 = index2;
            index2 = null;
            object = iLookupThunk10.get(object28);
            if (iLookupThunk10 == object) {
                __thunk__9__ = __site__9__.fault(object28);
                object = __thunk__9__.get(object28);
            }
        } else {
            object = null;
        }
        Object object29 = d;
        d = null;
        return iFn.invoke((Object)integrity$avet_dquark_seq$fn__22361, iFn2.invoke(object6, (Object)iObj, iFn3.invoke((Object)integrity$avet_dquark_seq$fn__22366, (Object)Tuple.create((Object)iPersistentVector, (Object)iPersistentVector2, (Object)iPersistentVector3, (Object)iPersistentVector4, (Object)Tuple.create((Object)const__14, (Object)iFn8.invoke(object, object29))))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$avet_dquark_seq.invokeStatic(object3, object4);
    }
}


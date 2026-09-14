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
import datomic.integrity$validate_fulltext$fn__22428;
import datomic.integrity$validate_fulltext$progress__22426;

public final class integrity$validate_fulltext
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"datomic.integrity", (String)"fulltext-path-reachability");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__11 = RT.keyword(null, (String)"paths");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"root"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"root"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object db2, Object olookup) {
        Object temp__5457__auto__22431;
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object count2;
        Object object5 = count2 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        count2 = null;
        integrity$validate_fulltext$progress__22426 progress = new integrity$validate_fulltext$progress__22426(object5);
        IFn iFn = (IFn)const__2.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        integrity$validate_fulltext$fn__22428 integrity$validate_fulltext$fn__22428 = new integrity$validate_fulltext$fn__22428();
        IFn iFn3 = (IFn)const__4.getRawRoot();
        IFn iFn4 = (IFn)const__5.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__2__;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        ILookupThunk iLookupThunk3 = __thunk__0__;
        Object object6 = db2;
        Object object7 = iLookupThunk3.get(object6);
        if (iLookupThunk3 == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        if (iLookupThunk2 == (object4 = iLookupThunk2.get(object7))) {
            __thunk__1__ = __site__1__.fault(object7);
            object4 = __thunk__1__.get(object7);
        }
        if (iLookupThunk == (object3 = iLookupThunk.get(object4))) {
            __thunk__2__ = __site__2__.fault(object4);
            object3 = __thunk__2__.get(object4);
        }
        Object object8 = iFn4.invoke(object3, db2, olookup, (Object)progress);
        IFn iFn5 = (IFn)const__5.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__5__;
        ILookupThunk iLookupThunk5 = __thunk__4__;
        ILookupThunk iLookupThunk6 = __thunk__3__;
        Object object9 = db2;
        Object object10 = iLookupThunk6.get(object9);
        if (iLookupThunk6 == object10) {
            __thunk__3__ = __site__3__.fault(object9);
            object10 = __thunk__3__.get(object9);
        }
        if (iLookupThunk5 == (object2 = iLookupThunk5.get(object10))) {
            __thunk__4__ = __site__4__.fault(object10);
            object2 = __thunk__4__.get(object10);
        }
        if (iLookupThunk4 == (object = iLookupThunk4.get(object2))) {
            __thunk__5__ = __site__5__.fault(object2);
            object = __thunk__5__.get(object2);
        }
        Object object11 = db2;
        db2 = null;
        Object object12 = olookup;
        olookup = null;
        integrity$validate_fulltext$progress__22426 integrity$validate_fulltext$progress__22426 = progress;
        progress = null;
        Object object13 = temp__5457__auto__22431 = iFn.invoke(iFn2.invoke((Object)integrity$validate_fulltext$fn__22428, iFn3.invoke(object8, iFn5.invoke(object, object11, object12, (Object)integrity$validate_fulltext$progress__22426))));
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = temp__5457__auto__22431;
            temp__5457__auto__22431 = null;
            Object missing = object14;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__11;
            Object object15 = missing;
            missing = null;
            objectArray[1] = object15;
            throw (Throwable)((IFn)const__10.getRawRoot()).invoke((Object)"Some fulltext paths are missing", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$validate_fulltext.invokeStatic(object3, object4);
    }
}


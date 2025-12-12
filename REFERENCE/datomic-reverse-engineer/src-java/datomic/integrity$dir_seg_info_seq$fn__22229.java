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

public final class integrity$dir_seg_info_seq$fn__22229
extends AFunction {
    Object olookup;
    public static final Keyword const__0 = RT.keyword(null, (String)"dir-t");
    public static final Keyword const__2 = RT.keyword(null, (String)"segid");
    public static final Keyword const__4 = RT.keyword(null, (String)"seg-t");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public integrity$dir_seg_info_seq$fn__22229(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object d) {
        Object[] objectArray = new Object[6];
        objectArray[0] = const__0;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = d;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        objectArray[1] = object2;
        objectArray[2] = const__2;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = d;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        objectArray[3] = object4;
        objectArray[4] = const__4;
        ILookupThunk iLookupThunk3 = __thunk__3__;
        IFn iFn = (IFn)const__5.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__2__;
        Object object5 = d;
        d = null;
        Object object6 = iLookupThunk4.get(object5);
        if (iLookupThunk4 == object6) {
            __thunk__2__ = __site__2__.fault(object5);
            object6 = __thunk__2__.get(object5);
        }
        Object object7 = iFn.invoke(RT.get((Object)this.olookup, (Object)object6));
        Object object8 = iLookupThunk3.get(object7);
        if (iLookupThunk3 == object8) {
            __thunk__3__ = __site__3__.fault(object7);
            object8 = __thunk__3__.get(object7);
        }
        objectArray[5] = object8;
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}


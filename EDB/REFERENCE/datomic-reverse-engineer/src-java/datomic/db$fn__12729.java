/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.Writer;

public final class db$fn__12729
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"print-method");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"ident"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"value-type"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"indexed"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"has-avet"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"unique"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"is-component"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"no-history"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__9__ = __site__9__;

    public static Object invokeStatic(Object ai, Object w) {
        ((Writer)w).write("#AttrInfo{");
        ((Writer)w).write(":id ");
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = ai;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        iFn.invoke(object2, w);
        ((Writer)w).write(" :ident ");
        IFn iFn2 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = ai;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        iFn2.invoke(object4, w);
        ((Writer)w).write(" :value-type ");
        IFn iFn3 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object5 = ai;
        Object object6 = iLookupThunk3.get(object5);
        if (iLookupThunk3 == object6) {
            __thunk__2__ = __site__2__.fault(object5);
            object6 = __thunk__2__.get(object5);
        }
        iFn3.invoke(object6, w);
        ((Writer)w).write(" :cardinality ");
        IFn iFn4 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object7 = ai;
        Object object8 = iLookupThunk4.get(object7);
        if (iLookupThunk4 == object8) {
            __thunk__3__ = __site__3__.fault(object7);
            object8 = __thunk__3__.get(object7);
        }
        iFn4.invoke(object8, w);
        ((Writer)w).write(" :indexed ");
        IFn iFn5 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object9 = ai;
        Object object10 = iLookupThunk5.get(object9);
        if (iLookupThunk5 == object10) {
            __thunk__4__ = __site__4__.fault(object9);
            object10 = __thunk__4__.get(object9);
        }
        iFn5.invoke(object10, w);
        ((Writer)w).write(" :has-avet ");
        IFn iFn6 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk6 = __thunk__5__;
        Object object11 = ai;
        Object object12 = iLookupThunk6.get(object11);
        if (iLookupThunk6 == object12) {
            __thunk__5__ = __site__5__.fault(object11);
            object12 = __thunk__5__.get(object11);
        }
        iFn6.invoke(object12, w);
        ((Writer)w).write(" :unique ");
        IFn iFn7 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk7 = __thunk__6__;
        Object object13 = ai;
        Object object14 = iLookupThunk7.get(object13);
        if (iLookupThunk7 == object14) {
            __thunk__6__ = __site__6__.fault(object13);
            object14 = __thunk__6__.get(object13);
        }
        iFn7.invoke(object14, w);
        ((Writer)w).write(" :is-component ");
        IFn iFn8 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk8 = __thunk__7__;
        Object object15 = ai;
        Object object16 = iLookupThunk8.get(object15);
        if (iLookupThunk8 == object16) {
            __thunk__7__ = __site__7__.fault(object15);
            object16 = __thunk__7__.get(object15);
        }
        iFn8.invoke(object16, w);
        ((Writer)w).write(" :no-history ");
        IFn iFn9 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk9 = __thunk__8__;
        Object object17 = ai;
        Object object18 = iLookupThunk9.get(object17);
        if (iLookupThunk9 == object18) {
            __thunk__8__ = __site__8__.fault(object17);
            object18 = __thunk__8__.get(object17);
        }
        iFn9.invoke(object18, w);
        ((Writer)w).write(" :fulltext ");
        IFn iFn10 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk10 = __thunk__9__;
        Object object19 = ai;
        ai = null;
        Object object20 = iLookupThunk10.get(object19);
        if (iLookupThunk10 == object20) {
            __thunk__9__ = __site__9__.fault(object19);
            object20 = __thunk__9__.get(object19);
        }
        iFn10.invoke(object20, w);
        Object object21 = w;
        w = null;
        ((Writer)object21).write("}");
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$fn__12729.invokeStatic(object3, object4);
    }
}


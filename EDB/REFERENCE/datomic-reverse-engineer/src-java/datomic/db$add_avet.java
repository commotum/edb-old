/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
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
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$add_avet$fn__13199;
import datomic.db.Attribute;

public final class db$add_avet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"set-element-fields");
    public static final Keyword const__2 = RT.keyword(null, (String)"index");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__6 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__7 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__8 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__11 = RT.keyword(null, (String)"a");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final AFn const__13 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memidx"), (Object)RT.keyword(null, (String)"avet"));
    public static final Keyword const__14 = RT.keyword(null, (String)"needsAVET");
    public static final Keyword const__15 = RT.keyword(null, (String)"storageHasAVET");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"can-immediately-toggle-storage-has-avet?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object db2, Object aid, Object _, Object _2) {
        IPersistentVector iPersistentVector;
        Object object = ((Attribute)((IFn)db$add_avet.const__0.getRawRoot()).invoke((Object)db2, (Object)aid)).unique;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = db2;
            db2 = null;
            Object object3 = aid;
            aid = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(object2, object3, (Object)const__2, (Object)Boolean.TRUE));
        } else {
            Object avet2;
            Object object4;
            Object object5;
            IFn iFn = (IFn)const__3.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__1__;
            ILookupThunk iLookupThunk2 = __thunk__0__;
            Object object6 = db2;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            if (iLookupThunk == (object5 = iLookupThunk.get(object7))) {
                __thunk__1__ = __site__1__.fault(object7);
                object5 = __thunk__1__.get(object7);
            }
            IFn iFn2 = (IFn)const__6.getRawRoot();
            IFn iFn3 = (IFn)const__7.getRawRoot();
            db$add_avet$fn__13199 db$add_avet$fn__13199 = new db$add_avet$fn__13199(aid);
            IFn iFn4 = (IFn)const__8.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__3__;
            ILookupThunk iLookupThunk4 = __thunk__2__;
            Object object8 = db2;
            Object object9 = iLookupThunk4.get(object8);
            if (iLookupThunk4 == object9) {
                __thunk__2__ = __site__2__.fault(object8);
                object9 = __thunk__2__.get(object8);
            }
            if (iLookupThunk3 == (object4 = iLookupThunk3.get(object9))) {
                __thunk__3__ = __site__3__.fault(object9);
                object4 = __thunk__3__.get(object9);
            }
            Object object10 = avet2 = iFn.invoke(object5, iFn2.invoke(iFn3.invoke((Object)db$add_avet$fn__13199, iFn4.invoke(object4, ((IFn)const__10.getRawRoot()).invoke(db2, (Object)const__11, aid)))));
            avet2 = null;
            Object object11 = ((IFn)const__12.getRawRoot()).invoke(db2, (Object)const__13, object10);
            Object object12 = aid;
            Object object13 = db2;
            db2 = null;
            Object object14 = aid;
            aid = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(object11, object12, (Object)const__2, (Object)Boolean.TRUE, (Object)const__14, (Object)Boolean.TRUE, (Object)const__15, ((IFn)const__16.getRawRoot()).invoke(object13, object14)));
        }
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$add_avet.invokeStatic(object5, object6, object7, object8);
    }
}


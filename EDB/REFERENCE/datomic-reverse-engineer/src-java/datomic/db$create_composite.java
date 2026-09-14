/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
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
import datomic.Database;
import datomic.db$create_composite$fn__13925;

public final class db$create_composite
extends AFunction {
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"ea->v");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"asserting-datum");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleAttrs"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object eaop_map, Object p__13921) {
        Object object;
        Object object2 = p__13921;
        p__13921 = null;
        Object vec__13922 = object2;
        Object e = RT.nth((Object)vec__13922, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__13922;
        vec__13922 = null;
        Object a = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(db2, a);
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        Object attrs = object5;
        long t = ((Database)db2).nextT();
        Object object6 = eaop_map;
        eaop_map = null;
        Object object7 = attrs;
        attrs = null;
        Object v = ((IFn)const__5.getRawRoot()).invoke((Object)new db$create_composite$fn__13925(t, object6, e, db2), object7);
        Object object8 = ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), v);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object temp__5457__auto__13929;
            Object object9 = db2;
            db2 = null;
            Object object10 = temp__5457__auto__13929 = ((IFn)const__8.getRawRoot()).invoke(object9, e, a);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object11 = temp__5457__auto__13929;
                temp__5457__auto__13929 = null;
                Object composite_v = object11;
                Object object12 = e;
                e = null;
                Object object13 = a;
                a = null;
                Object object14 = composite_v;
                composite_v = null;
                object = ((IFn.LLOLO)const__9.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object12)), RT.uncheckedLongCast((Object)((Number)object13)), object14, t);
            } else {
                object = null;
            }
        } else {
            Object object15 = e;
            e = null;
            Object object16 = a;
            a = null;
            Object object17 = v;
            v = null;
            object = ((IFn.LLOLO)const__10.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object15)), RT.uncheckedLongCast((Object)((Number)object16)), object17, t);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$create_composite.invokeStatic(object4, object5, object6);
    }
}


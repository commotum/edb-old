/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$datoms$fn__12930;
import datomic.db$datoms$fn__12932;
import datomic.db$datoms$fn__12934;
import datomic.db$datoms$fn__12936;
import datomic.db$datoms$fn__12938;
import datomic.db.Attribute;

public final class db$datoms
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Object const__2 = 0L;
    public static final Keyword const__6 = RT.keyword(null, (String)"avet");
    public static final Keyword const__7 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__8 = RT.keyword(null, (String)"vaet");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"maybe-require-ids");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__15 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__16 = RT.keyword((String)"db.error", (String)"attribute-not-indexed");
    public static final Var const__17 = RT.var((String)"datomic.iter", (String)"iterable");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object index2, Object components) {
        Object object;
        AFunction aFunction;
        Number t;
        Object v;
        Object e;
        Object a;
        block24: {
            Number number;
            Object v2;
            Object object2;
            block23: {
                Object G__12919 = index2;
                switch (Util.hash((Object)G__12919) >> 5 & 3) {
                    case 0: {
                        if (G__12919 != const__0) break;
                        Object object3 = components;
                        components = null;
                        Object vec__12920 = object3;
                        a = RT.nth((Object)vec__12920, (int)RT.uncheckedIntCast((long)0L), null);
                        Object e2 = RT.nth((Object)vec__12920, (int)RT.uncheckedIntCast((long)1L), null);
                        Object v3 = RT.nth((Object)vec__12920, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object4 = vec__12920;
                        vec__12920 = null;
                        Object t2 = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object5 = e2;
                        e2 = null;
                        Object object6 = a;
                        a = null;
                        Object object7 = v3;
                        v3 = null;
                        Object object8 = t2;
                        t2 = null;
                        object2 = Tuple.create((Object)object5, (Object)object6, (Object)object7, (Object)object8);
                        break block23;
                    }
                    case 1: {
                        if (G__12919 != const__6) break;
                        Object object9 = components;
                        components = null;
                        Object vec__12923 = object9;
                        a = RT.nth((Object)vec__12923, (int)RT.uncheckedIntCast((long)0L), null);
                        v2 = RT.nth((Object)vec__12923, (int)RT.uncheckedIntCast((long)1L), null);
                        Object e3 = RT.nth((Object)vec__12923, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object10 = vec__12923;
                        vec__12923 = null;
                        Object t2 = RT.nth((Object)object10, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object11 = e3;
                        e3 = null;
                        Object object12 = a;
                        a = null;
                        Object object13 = v2;
                        v2 = null;
                        Object object14 = t2;
                        t2 = null;
                        object2 = Tuple.create((Object)object11, (Object)object12, (Object)object13, (Object)object14);
                        break block23;
                    }
                    case 2: {
                        if (G__12919 != const__7) break;
                        object2 = components;
                        components = null;
                        break block23;
                    }
                    case 3: {
                        if (G__12919 != const__8) break;
                        Object object15 = components;
                        components = null;
                        Object vec__12926 = object15;
                        Object v4 = RT.nth((Object)vec__12926, (int)RT.uncheckedIntCast((long)0L), null);
                        Object a2 = RT.nth((Object)vec__12926, (int)RT.uncheckedIntCast((long)1L), null);
                        Object e3 = RT.nth((Object)vec__12926, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object16 = vec__12926;
                        vec__12926 = null;
                        Object t2 = RT.nth((Object)object16, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object17 = e3;
                        e3 = null;
                        Object object18 = a2;
                        a2 = null;
                        Object object19 = v4;
                        v4 = null;
                        Object object20 = t2;
                        t2 = null;
                        object2 = Tuple.create((Object)object17, (Object)object18, (Object)object19, (Object)object20);
                        break block23;
                    }
                }
                Object object21 = G__12919;
                G__12919 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object21));
            }
            Object vec__12916 = object2;
            e = RT.nth((Object)vec__12916, (int)RT.uncheckedIntCast((long)0L), null);
            a = RT.nth((Object)vec__12916, (int)RT.uncheckedIntCast((long)1L), null);
            v2 = RT.nth((Object)vec__12916, (int)RT.uncheckedIntCast((long)2L), null);
            Object object22 = vec__12916;
            vec__12916 = null;
            Object t3 = RT.nth((Object)object22, (int)RT.uncheckedIntCast((long)3L), null);
            Object object23 = v2;
            v2 = null;
            v = ((IFn)const__10.getRawRoot()).invoke(db2, a, object23, (Object)(Util.equiv((Object)index2, (Object)const__8) ? Boolean.TRUE : Boolean.FALSE));
            Object object24 = t3;
            if (object24 != null && object24 != Boolean.FALSE) {
                Object object25 = t3;
                t3 = null;
                number = Numbers.num((long)((IFn.LL)const__12.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object25))));
            } else {
                number = null;
            }
            t = number;
            Object G__12929 = index2;
            switch (Util.hash((Object)G__12929) >> 5 & 3) {
                case 0: {
                    if (G__12929 != const__0) break;
                    aFunction = new db$datoms$fn__12930();
                    break block24;
                }
                case 1: {
                    if (G__12929 != const__6) break;
                    aFunction = new db$datoms$fn__12932();
                    break block24;
                }
                case 2: {
                    if (G__12929 != const__7) break;
                    aFunction = new db$datoms$fn__12934();
                    break block24;
                }
                case 3: {
                    if (G__12929 != const__8) break;
                    aFunction = new db$datoms$fn__12936();
                    break block24;
                }
            }
            Object object26 = G__12929;
            G__12929 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object26));
        }
        AFunction op = aFunction;
        Object object27 = e;
        Object eid = object27 != null && object27 != Boolean.FALSE ? ((IFn)const__13.getRawRoot()).invoke(db2, e) : const__2;
        Object object28 = a;
        Object attrid = object28 != null && object28 != Boolean.FALSE ? ((IFn)const__13.getRawRoot()).invoke(db2, a) : const__2;
        Object object29 = index2;
        index2 = null;
        boolean and__5236__auto__12951 = Util.equiv((Object)const__6, (Object)object29);
        if (and__5236__auto__12951) {
            Object and__5236__auto__12950;
            Object object30 = and__5236__auto__12950 = a;
            if (object30 != null && object30 != Boolean.FALSE) {
                object = attrid;
            } else {
                object = and__5236__auto__12950;
                and__5236__auto__12950 = null;
            }
        } else {
            object = and__5236__auto__12951 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (object != null && object != Boolean.FALSE) {
            Object temp__5457__auto__12952;
            Object object31 = temp__5457__auto__12952 = ((IFn)const__14.getRawRoot()).invoke(db2, attrid);
            if (object31 != null && object31 != Boolean.FALSE) {
                Object attr;
                Object object32 = temp__5457__auto__12952;
                temp__5457__auto__12952 = null;
                Object object33 = attr = object32;
                attr = null;
                Object object34 = ((Attribute)object33).hasAVET();
                if (object34 != null && object34 != Boolean.FALSE) {
                } else {
                    ((IFn)const__15.getRawRoot()).invoke((Object)const__16, ((IFn)const__9.getRawRoot()).invoke((Object)"attribute: ", a, (Object)" is not indexed"));
                }
            }
        }
        Object object35 = db2;
        db2 = null;
        Number number = t;
        t = null;
        Object object36 = attrid;
        attrid = null;
        Object object37 = eid;
        eid = null;
        Object object38 = v;
        v = null;
        AFunction aFunction2 = op;
        op = null;
        Object object39 = a;
        a = null;
        return ((IFn)const__17.getRawRoot()).invoke((Object)new db$datoms$fn__12938(object35, number, object36, object37, object38, aFunction2, object39, e));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$datoms.invokeStatic(object4, object5, object6);
    }
}


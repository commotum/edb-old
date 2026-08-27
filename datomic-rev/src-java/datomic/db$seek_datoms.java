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
import datomic.db$seek_datoms$fn__12829;
import datomic.db$seek_datoms$fn__12831;
import datomic.db$seek_datoms$fn__12833;
import datomic.db$seek_datoms$fn__12835;
import datomic.db$seek_datoms$fn__12837;
import datomic.db.Attribute;

public final class db$seek_datoms
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
        Object object2;
        AFunction aFunction;
        Number t;
        Object v;
        Object e;
        Object a;
        block26: {
            Number number;
            Object v2;
            Object object3;
            block25: {
                Object G__12818 = index2;
                switch (Util.hash((Object)G__12818) >> 5 & 3) {
                    case 0: {
                        if (G__12818 != const__0) break;
                        Object object4 = components;
                        components = null;
                        Object vec__12819 = object4;
                        a = RT.nth((Object)vec__12819, (int)RT.uncheckedIntCast((long)0L), null);
                        Object e2 = RT.nth((Object)vec__12819, (int)RT.uncheckedIntCast((long)1L), null);
                        Object v3 = RT.nth((Object)vec__12819, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object5 = vec__12819;
                        vec__12819 = null;
                        Object t2 = RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object6 = e2;
                        e2 = null;
                        Object object7 = a;
                        a = null;
                        Object object8 = v3;
                        v3 = null;
                        Object object9 = t2;
                        t2 = null;
                        object3 = Tuple.create((Object)object6, (Object)object7, (Object)object8, (Object)object9);
                        break block25;
                    }
                    case 1: {
                        if (G__12818 != const__6) break;
                        Object object10 = components;
                        components = null;
                        Object vec__12822 = object10;
                        a = RT.nth((Object)vec__12822, (int)RT.uncheckedIntCast((long)0L), null);
                        v2 = RT.nth((Object)vec__12822, (int)RT.uncheckedIntCast((long)1L), null);
                        Object e3 = RT.nth((Object)vec__12822, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object11 = vec__12822;
                        vec__12822 = null;
                        Object t2 = RT.nth((Object)object11, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object12 = e3;
                        e3 = null;
                        Object object13 = a;
                        a = null;
                        Object object14 = v2;
                        v2 = null;
                        Object object15 = t2;
                        t2 = null;
                        object3 = Tuple.create((Object)object12, (Object)object13, (Object)object14, (Object)object15);
                        break block25;
                    }
                    case 2: {
                        if (G__12818 != const__7) break;
                        object3 = components;
                        components = null;
                        break block25;
                    }
                    case 3: {
                        if (G__12818 != const__8) break;
                        Object object16 = components;
                        components = null;
                        Object vec__12825 = object16;
                        Object v4 = RT.nth((Object)vec__12825, (int)RT.uncheckedIntCast((long)0L), null);
                        Object a2 = RT.nth((Object)vec__12825, (int)RT.uncheckedIntCast((long)1L), null);
                        Object e3 = RT.nth((Object)vec__12825, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object17 = vec__12825;
                        vec__12825 = null;
                        Object t2 = RT.nth((Object)object17, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object18 = e3;
                        e3 = null;
                        Object object19 = a2;
                        a2 = null;
                        Object object20 = v4;
                        v4 = null;
                        Object object21 = t2;
                        t2 = null;
                        object3 = Tuple.create((Object)object18, (Object)object19, (Object)object20, (Object)object21);
                        break block25;
                    }
                }
                Object object22 = G__12818;
                G__12818 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object22));
            }
            Object vec__12815 = object3;
            e = RT.nth((Object)vec__12815, (int)RT.uncheckedIntCast((long)0L), null);
            a = RT.nth((Object)vec__12815, (int)RT.uncheckedIntCast((long)1L), null);
            v2 = RT.nth((Object)vec__12815, (int)RT.uncheckedIntCast((long)2L), null);
            Object object23 = vec__12815;
            vec__12815 = null;
            Object t3 = RT.nth((Object)object23, (int)RT.uncheckedIntCast((long)3L), null);
            Object object24 = v2;
            v2 = null;
            v = ((IFn)const__10.getRawRoot()).invoke(db2, a, object24, (Object)(Util.equiv((Object)index2, (Object)const__8) ? Boolean.TRUE : Boolean.FALSE));
            Object object25 = t3;
            if (object25 != null && object25 != Boolean.FALSE) {
                Object object26 = t3;
                t3 = null;
                number = Numbers.num((long)((IFn.LL)const__12.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object26))));
            } else {
                number = null;
            }
            t = number;
            Object G__12828 = index2;
            switch (Util.hash((Object)G__12828) >> 5 & 3) {
                case 0: {
                    if (G__12828 != const__0) break;
                    aFunction = new db$seek_datoms$fn__12829();
                    break block26;
                }
                case 1: {
                    if (G__12828 != const__6) break;
                    aFunction = new db$seek_datoms$fn__12831();
                    break block26;
                }
                case 2: {
                    if (G__12828 != const__7) break;
                    aFunction = new db$seek_datoms$fn__12833();
                    break block26;
                }
                case 3: {
                    if (G__12828 != const__8) break;
                    aFunction = new db$seek_datoms$fn__12835();
                    break block26;
                }
            }
            Object object27 = G__12828;
            G__12828 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object27));
        }
        AFunction op = aFunction;
        Object object28 = e;
        if (object28 != null && object28 != Boolean.FALSE) {
            Object object29 = e;
            e = null;
            object2 = ((IFn)const__13.getRawRoot()).invoke(db2, object29);
        } else {
            object2 = const__2;
        }
        Object eid = object2;
        Object object30 = a;
        Object attrid = object30 != null && object30 != Boolean.FALSE ? ((IFn)const__13.getRawRoot()).invoke(db2, a) : const__2;
        Object object31 = index2;
        index2 = null;
        boolean and__5236__auto__12841 = Util.equiv((Object)const__6, (Object)object31);
        if (and__5236__auto__12841) {
            Object and__5236__auto__12840;
            Object object32 = and__5236__auto__12840 = a;
            if (object32 != null && object32 != Boolean.FALSE) {
                object = attrid;
            } else {
                object = and__5236__auto__12840;
                and__5236__auto__12840 = null;
            }
        } else {
            object = and__5236__auto__12841 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (object != null && object != Boolean.FALSE) {
            Object temp__5457__auto__12842;
            Object object33 = temp__5457__auto__12842 = ((IFn)const__14.getRawRoot()).invoke(db2, attrid);
            if (object33 != null && object33 != Boolean.FALSE) {
                Object attr;
                Object object34 = temp__5457__auto__12842;
                temp__5457__auto__12842 = null;
                Object object35 = attr = object34;
                attr = null;
                Object object36 = ((Attribute)object35).hasAVET();
                if (object36 != null && object36 != Boolean.FALSE) {
                } else {
                    Object object37 = a;
                    a = null;
                    ((IFn)const__15.getRawRoot()).invoke((Object)const__16, ((IFn)const__9.getRawRoot()).invoke((Object)"attribute: ", object37, (Object)" is not indexed"));
                }
            }
        }
        Object object38 = eid;
        eid = null;
        Object object39 = attrid;
        attrid = null;
        AFunction aFunction2 = op;
        op = null;
        Object object40 = v;
        v = null;
        Number number = t;
        t = null;
        return ((IFn)const__17.getRawRoot()).invoke((Object)new db$seek_datoms$fn__12837(object38, object39, aFunction2, object40, number, db2));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$seek_datoms.invokeStatic(object4, object5, object6);
    }
}


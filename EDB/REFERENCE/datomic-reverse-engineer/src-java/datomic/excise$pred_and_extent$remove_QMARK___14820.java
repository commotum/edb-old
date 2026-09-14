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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class excise$pred_and_extent$remove_QMARK___14820
extends AFunction {
    Object component_QMARK_;
    Object before_t;
    Object ref_QMARK_;
    long t;
    Object target;
    Object attrs;
    Object type;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"keeper?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Keyword const__7 = RT.keyword(null, (String)"e");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");

    public excise$pred_and_extent$remove_QMARK___14820(Object object, Object object2, Object object3, long l, Object object4, Object object5, Object object6) {
        this.component_QMARK_ = object;
        this.before_t = object2;
        this.ref_QMARK_ = object3;
        this.t = l;
        this.target = object4;
        this.attrs = object5;
        this.type = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object d) {
        block21: {
            block20: {
                v0 = and__5236__auto__14825 = ((IFn)excise$pred_and_extent$remove_QMARK___14820.const__0.getRawRoot()).invoke(((IFn)excise$pred_and_extent$remove_QMARK___14820.const__1.getRawRoot()).invoke(d));
                if (v0 != null && v0 != Boolean.FALSE) {
                    and__5236__auto__14824 = Numbers.lt((long)((IDatum)d).getT(), (long)this.t);
                    v1 = and__5236__auto__14824 ? ((or__5238__auto__14823 = Util.identical((Object)this.before_t, null)) ? (or__5238__auto__14823 ? Boolean.TRUE : Boolean.FALSE) : (Numbers.lt((long)((IDatum)d).getT(), (long)((IFn.LL)excise$pred_and_extent$remove_QMARK___14820.const__4.getRawRoot()).invokePrim(RT.longCast((Object)((Number)this.before_t)))) ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__14824 ? Boolean.TRUE : Boolean.FALSE);
                } else {
                    v1 = and__5236__auto__14825;
                    and__5236__auto__14825 = null;
                }
                if (v1 == null || v1 == Boolean.FALSE) break block20;
                G__14821 = this.type;
                switch (Util.hash((Object)G__14821)) {
                    case 1013910569: {
                        if (G__14821 == excise$pred_and_extent$remove_QMARK___14820.const__5) {
                            v2 = d;
                            d = null;
                            this = null;
                            if (Util.equiv((Object)this.target, (long)((IDatum)v2).getA())) {
                                v3 = Boolean.TRUE;
                                break;
                            }
                            v3 = Boolean.FALSE;
                            break;
                        }
                        ** GOTO lbl65
                    }
                    case 1013910832: {
                        if (G__14821 == excise$pred_and_extent$remove_QMARK___14820.const__7) {
                            or__5238__auto__14827 = Util.equiv((Object)this.target, (long)((IDatum)d).getE());
                            if (or__5238__auto__14827) {
                                v4 = or__5238__auto__14827 ? Boolean.TRUE : Boolean.FALSE;
                            } else {
                                v5 = and__5236__auto__14826 = ((IFn)this.ref_QMARK_).invoke((Object)((IDatum)d).getA());
                                if (v5 != null && v5 != Boolean.FALSE) {
                                    v4 = Util.equiv((Object)this.target, (Object)((IDatum)d).getV()) ? Boolean.TRUE : Boolean.FALSE;
                                } else {
                                    v4 = and__5236__auto__14826;
                                    and__5236__auto__14826 = null;
                                }
                            }
                            v6 = and__5236__auto__14829 = v4;
                            if (v6 != null && v6 != Boolean.FALSE) {
                                v7 = or__5238__auto__14828 = ((IFn)excise$pred_and_extent$remove_QMARK___14820.const__8.getRawRoot()).invoke(this.attrs);
                                if (v7 != null && v7 != Boolean.FALSE) {
                                    v8 = or__5238__auto__14828;
                                    or__5238__auto__14828 = null;
                                } else {
                                    v8 = ((IFn)excise$pred_and_extent$remove_QMARK___14820.const__9.getRawRoot()).invoke(this.attrs, (Object)((IDatum)d).getA());
                                }
                            } else {
                                v8 = and__5236__auto__14829;
                                or__5238__auto__14832 = null;
                            }
                            v9 = or__5238__auto__14832 = v8;
                            if (v9 != null && v9 != Boolean.FALSE) {
                                v3 = or__5238__auto__14832;
                                or__5238__auto__14832 = null;
                                break;
                            }
                            v10 = or__5238__auto__14831 = ((IFn)this.component_QMARK_).invoke((Object)Numbers.num((long)((IDatum)d).getE()));
                            if (v10 != null && v10 != Boolean.FALSE) {
                                v3 = or__5238__auto__14831;
                                or__5238__auto__14831 = null;
                                break;
                            }
                            v11 = and__5236__auto__14830 = ((IFn)this.ref_QMARK_).invoke((Object)((IDatum)d).getA());
                            if (v11 != null && v11 != Boolean.FALSE) {
                                v12 = d;
                                d = null;
                                this = null;
                                v3 = ((IFn)this.component_QMARK_).invoke(((IDatum)v12).getV());
                                break;
                            }
                            v3 = and__5236__auto__14830;
                            and__5236__auto__14830 = null;
                            break;
                        }
                    }
lbl65:
                    // 4 sources

                    default: {
                        v13 = G__14821;
                        G__14821 = null;
                        throw (Throwable)new IllegalArgumentException((String)((IFn)excise$pred_and_extent$remove_QMARK___14820.const__10.getRawRoot()).invoke((Object)"No matching clause: ", v13));
                    }
                }
                break block21;
            }
            v3 = null;
        }
        return v3;
    }
}


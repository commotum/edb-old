/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
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

public final class index$sparse_datom
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Keyword const__2 = RT.keyword(null, (String)"avet");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__4 = 0L;
    public static final Keyword const__5 = RT.keyword(null, (String)"else");
    public static final Var const__6 = RT.var((String)"datomic.index", (String)"mindiff");
    public static final Keyword const__7 = RT.keyword(null, (String)"raet");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__10 = RT.var((String)"datomic.common", (String)"compare");
    public static final Keyword const__11 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__12 = RT.keyword(null, (String)"aevt");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object idx, Object prior, Object d) {
        Object maked = ((IDatum)d).isAssertion() ? const__0.getRawRoot() : const__1.getRawRoot();
        Object object = idx;
        idx = null;
        Object G__15395 = object;
        switch (Util.hash((Object)G__15395) >> 20 & 3) {
            case 0: {
                Object object2;
                if (G__15395 != const__2) break;
                Object object3 = ((IFn)const__3.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object object4 = maked;
                    maked = null;
                    Object object5 = d;
                    d = null;
                    object2 = ((IFn)object4).invoke(const__4, (Object)((IDatum)object5).getA(), null, const__4);
                    return object2;
                }
                Keyword keyword = const__5;
                if (keyword == null) return null;
                if (keyword == Boolean.FALSE) return null;
                Object dv = ((IDatum)d).getV();
                Object object6 = prior;
                prior = null;
                Object diff_v = ((IFn)const__6.getRawRoot()).invoke(((IDatum)object6).getV(), dv);
                Object object7 = dv;
                dv = null;
                Object object8 = ((IFn)const__3.getRawRoot()).invoke(diff_v, object7);
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = maked;
                    maked = null;
                    Object object10 = d;
                    d = null;
                    Object object11 = diff_v;
                    diff_v = null;
                    object2 = ((IFn)object9).invoke(const__4, (Object)((IDatum)object10).getA(), object11, const__4);
                    return object2;
                }
                object2 = d;
                return object2;
            }
            case 1: {
                Object object2;
                if (G__15395 != const__7) break;
                Object object12 = ((IFn)const__8.getRawRoot()).invoke((Object)(Numbers.isZero((long)((IFn.OOL)const__10.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)prior).getV())) ? Boolean.TRUE : Boolean.FALSE));
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object object13 = maked;
                    maked = null;
                    Object object14 = d;
                    d = null;
                    object2 = ((IFn)object13).invoke(const__4, const__4, ((IDatum)object14).getV(), const__4);
                    return object2;
                }
                Object object15 = prior;
                prior = null;
                Object object16 = ((IFn)const__3.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)object15).getA());
                if (object16 != null && object16 != Boolean.FALSE) {
                    Object object17 = maked;
                    maked = null;
                    Integer n = ((IDatum)d).getA();
                    Object object18 = d;
                    d = null;
                    object2 = ((IFn)object17).invoke(const__4, (Object)n, ((IDatum)object18).getV(), const__4);
                    return object2;
                }
                Keyword keyword = const__5;
                if (keyword == null) return null;
                if (keyword == Boolean.FALSE) return null;
                object2 = d;
                return object2;
            }
            case 2: {
                Object object2;
                if (G__15395 != const__11) break;
                Object object19 = ((IFn)const__3.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getE()), (Object)Numbers.num((long)((IDatum)prior).getE()));
                if (object19 != null && object19 != Boolean.FALSE) {
                    Object object20 = maked;
                    maked = null;
                    Object object21 = d;
                    d = null;
                    object2 = ((IFn)object20).invoke((Object)Numbers.num((long)((IDatum)object21).getE()), const__4, null, const__4);
                    return object2;
                }
                Object object22 = ((IFn)const__3.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                if (object22 != null && object22 != Boolean.FALSE) {
                    Object object23 = maked;
                    maked = null;
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object24 = d;
                    d = null;
                    object2 = ((IFn)object23).invoke((Object)number, (Object)((IDatum)object24).getA(), null, const__4);
                    return object2;
                }
                Keyword keyword = const__5;
                if (keyword == null) return null;
                if (keyword == Boolean.FALSE) return null;
                Object dv = ((IDatum)d).getV();
                Object object25 = prior;
                prior = null;
                Object diff_v = ((IFn)const__6.getRawRoot()).invoke(((IDatum)object25).getV(), dv);
                Object object26 = dv;
                dv = null;
                Object object27 = ((IFn)const__3.getRawRoot()).invoke(diff_v, object26);
                if (object27 != null && object27 != Boolean.FALSE) {
                    Object object28 = maked;
                    maked = null;
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object29 = d;
                    d = null;
                    Object object30 = diff_v;
                    diff_v = null;
                    object2 = ((IFn)object28).invoke((Object)number, (Object)((IDatum)object29).getA(), object30, const__4);
                    return object2;
                }
                object2 = d;
                return object2;
            }
            case 3: {
                Object object2;
                if (G__15395 != const__12) break;
                Object object31 = ((IFn)const__3.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                if (object31 != null && object31 != Boolean.FALSE) {
                    Object object32 = maked;
                    maked = null;
                    Object object33 = d;
                    d = null;
                    object2 = ((IFn)object32).invoke(const__4, (Object)((IDatum)object33).getA(), null, const__4);
                    return object2;
                }
                Object object34 = ((IFn)const__3.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getE()), (Object)Numbers.num((long)((IDatum)prior).getE()));
                if (object34 != null && object34 != Boolean.FALSE) {
                    Object object35 = maked;
                    maked = null;
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object36 = d;
                    d = null;
                    object2 = ((IFn)object35).invoke((Object)number, (Object)((IDatum)object36).getA(), null, const__4);
                    return object2;
                }
                Keyword keyword = const__5;
                if (keyword == null) return null;
                if (keyword == Boolean.FALSE) return null;
                Object dv = ((IDatum)d).getV();
                Object object37 = prior;
                prior = null;
                Object diff_v = ((IFn)const__6.getRawRoot()).invoke(((IDatum)object37).getV(), dv);
                Object object38 = dv;
                dv = null;
                Object object39 = ((IFn)const__3.getRawRoot()).invoke(diff_v, object38);
                if (object39 != null && object39 != Boolean.FALSE) {
                    Object object40 = maked;
                    maked = null;
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object41 = d;
                    d = null;
                    Object object42 = diff_v;
                    diff_v = null;
                    object2 = ((IFn)object40).invoke((Object)number, (Object)((IDatum)object41).getA(), object42, const__4);
                    return object2;
                }
                object2 = d;
                return object2;
            }
        }
        Object object43 = G__15395;
        G__15395 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__13.getRawRoot()).invoke((Object)"No matching clause: ", object43));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$sparse_datom.invokeStatic(object4, object5, object6);
    }
}


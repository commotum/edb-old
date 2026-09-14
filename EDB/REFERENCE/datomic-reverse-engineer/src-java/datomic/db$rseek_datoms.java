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
import datomic.Database;
import datomic.db$rseek_datoms$fn__12899;
import datomic.db$rseek_datoms$fn__12901;
import datomic.db.Attribute;

public final class db$rseek_datoms
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Object const__2 = 0L;
    public static final Keyword const__6 = RT.keyword(null, (String)"avet");
    public static final Keyword const__7 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__8 = RT.keyword(null, (String)"vaet");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__13 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__14 = RT.keyword((String)"db.error", (String)"attribute-not-indexed");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"maybe-require-ids");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__18 = RT.var((String)"datomic.db", (String)"datum");
    public static final Var const__19 = RT.var((String)"datomic.db", (String)"reverse-datum-spec");
    public static final Keyword const__20 = RT.keyword(null, (String)"e");
    public static final Keyword const__21 = RT.keyword(null, (String)"a");
    public static final Keyword const__22 = RT.keyword(null, (String)"v");
    public static final Keyword const__23 = RT.keyword(null, (String)"t");
    public static final Var const__24 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__25 = RT.var((String)"datomic.db", (String)"rseek-index");
    public static final Var const__26 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__28 = RT.var((String)"datomic.db", (String)"distinct-last-by");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object index2, Object components) {
        Object object;
        Number number;
        Object object2;
        Object object3;
        Object v;
        Object object4;
        Object a;
        block19: {
            Object G__12889 = index2;
            switch (Util.hash((Object)G__12889) >> 5 & 3) {
                case 0: {
                    if (G__12889 != const__0) break;
                    Object object5 = components;
                    components = null;
                    Object vec__12890 = object5;
                    a = RT.nth((Object)vec__12890, (int)RT.uncheckedIntCast((long)0L), null);
                    Object e = RT.nth((Object)vec__12890, (int)RT.uncheckedIntCast((long)1L), null);
                    Object v2 = RT.nth((Object)vec__12890, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object6 = vec__12890;
                    vec__12890 = null;
                    Object t = RT.nth((Object)object6, (int)RT.uncheckedIntCast((long)3L), null);
                    Object object7 = e;
                    e = null;
                    Object object8 = a;
                    a = null;
                    Object object9 = v2;
                    v2 = null;
                    Object object10 = t;
                    t = null;
                    object4 = Tuple.create((Object)object7, (Object)object8, (Object)object9, (Object)object10);
                    break block19;
                }
                case 1: {
                    if (G__12889 != const__6) break;
                    Object object11 = components;
                    components = null;
                    Object vec__12893 = object11;
                    a = RT.nth((Object)vec__12893, (int)RT.uncheckedIntCast((long)0L), null);
                    v = RT.nth((Object)vec__12893, (int)RT.uncheckedIntCast((long)1L), null);
                    Object e = RT.nth((Object)vec__12893, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object12 = vec__12893;
                    vec__12893 = null;
                    Object t = RT.nth((Object)object12, (int)RT.uncheckedIntCast((long)3L), null);
                    Object object13 = e;
                    e = null;
                    Object object14 = a;
                    a = null;
                    Object object15 = v;
                    v = null;
                    Object object16 = t;
                    t = null;
                    object4 = Tuple.create((Object)object13, (Object)object14, (Object)object15, (Object)object16);
                    break block19;
                }
                case 2: {
                    if (G__12889 != const__7) break;
                    object4 = components;
                    components = null;
                    break block19;
                }
                case 3: {
                    if (G__12889 != const__8) break;
                    Object object17 = components;
                    components = null;
                    Object vec__12896 = object17;
                    Object v3 = RT.nth((Object)vec__12896, (int)RT.uncheckedIntCast((long)0L), null);
                    Object a2 = RT.nth((Object)vec__12896, (int)RT.uncheckedIntCast((long)1L), null);
                    Object e = RT.nth((Object)vec__12896, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object18 = vec__12896;
                    vec__12896 = null;
                    Object t = RT.nth((Object)object18, (int)RT.uncheckedIntCast((long)3L), null);
                    Object object19 = e;
                    e = null;
                    Object object20 = a2;
                    a2 = null;
                    Object object21 = v3;
                    v3 = null;
                    Object object22 = t;
                    t = null;
                    object4 = Tuple.create((Object)object19, (Object)object20, (Object)object21, (Object)object22);
                    break block19;
                }
            }
            Object object23 = G__12889;
            G__12889 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object23));
        }
        Object vec__12886 = object4;
        Object e = RT.nth((Object)vec__12886, (int)RT.uncheckedIntCast((long)0L), null);
        a = RT.nth((Object)vec__12886, (int)RT.uncheckedIntCast((long)1L), null);
        v = RT.nth((Object)vec__12886, (int)RT.uncheckedIntCast((long)2L), null);
        Object object24 = vec__12886;
        vec__12886 = null;
        Object t = RT.nth((Object)object24, (int)RT.uncheckedIntCast((long)3L), null);
        Object object25 = a;
        Object attrid = object25 != null && object25 != Boolean.FALSE ? ((IFn)const__10.getRawRoot()).invoke(db2, a) : const__2;
        boolean and__5236__auto__12905 = Util.equiv((Object)const__6, (Object)index2);
        if (and__5236__auto__12905) {
            Object and__5236__auto__12904;
            Object object26 = and__5236__auto__12904 = a;
            if (object26 != null && object26 != Boolean.FALSE) {
                object3 = attrid;
            } else {
                object3 = and__5236__auto__12904;
                and__5236__auto__12904 = null;
            }
        } else {
            object3 = and__5236__auto__12905 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object temp__5457__auto__12906;
            Object object27 = attrid;
            attrid = null;
            Object object28 = temp__5457__auto__12906 = ((IFn)const__12.getRawRoot()).invoke(db2, object27);
            if (object28 != null && object28 != Boolean.FALSE) {
                Object attr;
                Object object29 = temp__5457__auto__12906;
                temp__5457__auto__12906 = null;
                Object object30 = attr = object29;
                attr = null;
                Object object31 = ((Attribute)object30).hasAVET();
                object2 = object31 != null && object31 != Boolean.FALSE ? null : ((IFn)const__13.getRawRoot()).invoke((Object)const__14, ((IFn)const__9.getRawRoot()).invoke((Object)"attribute: ", a, (Object)" is not indexed"));
            } else {
                object2 = null;
            }
        } else {
            object2 = null;
        }
        Object object32 = v;
        v = null;
        Object v4 = ((IFn)const__15.getRawRoot()).invoke(db2, a, object32, (Object)(Util.equiv((Object)index2, (Object)const__8) ? Boolean.TRUE : Boolean.FALSE));
        Object object33 = t;
        if (object33 != null && object33 != Boolean.FALSE) {
            Object object34 = t;
            t = null;
            number = Numbers.num((long)((IFn.LL)const__16.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object34))));
        } else {
            number = null;
        }
        Number t2 = number;
        Object object35 = e;
        e = null;
        Object object36 = a;
        a = null;
        Object object37 = v4;
        v4 = null;
        Number number2 = t2;
        t2 = null;
        Object seek_d = ((IFn)const__17.getRawRoot()).invoke(const__18.getRawRoot(), db2, ((IFn)const__19.getRawRoot()).invoke(db2, index2, (Object)const__20, object35, (Object)const__21, object36, (Object)const__22, object37, (Object)const__23, (Object)number2));
        Object object38 = index2;
        index2 = null;
        Object object39 = seek_d;
        seek_d = null;
        Object datoms2 = ((IFn)const__24.getRawRoot()).invoke((Object)((Database)db2).history(), null, ((IFn)const__25.getRawRoot()).invoke(db2, object38, object39));
        Object object40 = db2;
        db2 = null;
        if (((Database)object40).isHistory()) {
            Object object41 = datoms2;
            datoms2 = null;
            object = ((IFn)const__26.getRawRoot()).invoke(object41);
            return object;
        }
        Object object42 = datoms2;
        datoms2 = null;
        object = ((IFn)const__27.getRawRoot()).invoke((Object)new db$rseek_datoms$fn__12899(), ((IFn)const__28.getRawRoot()).invoke((Object)new db$rseek_datoms$fn__12901(), ((IFn)const__26.getRawRoot()).invoke(object42)));
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$rseek_datoms.invokeStatic(object4, object5, object6);
    }
}


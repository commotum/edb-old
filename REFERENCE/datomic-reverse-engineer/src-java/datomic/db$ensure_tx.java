/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$ensure_tx
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"ensure-datom?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"ensure-entity!");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"attrPred"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"attrPred"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db_before, Object db_after, Object data2, Object idmap) {
        Object object = data2;
        data2 = null;
        Object seq_14082 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object chunk_14083 = null;
        long count_14084 = 0L;
        long i_14085 = 0L;
        while (true) {
            Object temp__5457__auto__14092;
            if (i_14085 < count_14084) {
                Object d = ((Indexed)chunk_14083).nth(RT.uncheckedIntCast((long)i_14085));
                Object object2 = ((IFn)const__3.getRawRoot()).invoke(db_before, d);
                if (object2 != null && object2 != Boolean.FALSE) {
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object3 = d;
                    d = null;
                    ((IFn)const__4.getRawRoot()).invoke(db_before, db_after, (Object)number, ((IDatum)object3).getV(), idmap);
                } else if (((IDatum)d).isAssertion()) {
                    Object temp__5457__auto__14089;
                    Object object4;
                    Object G__14086;
                    Object object5;
                    Object attr;
                    Object object6 = attr = ((IDbImpl)db_before).elementAt(((IDatum)d).getA());
                    attr = null;
                    Object G__140862 = object6;
                    if (Util.identical((Object)G__140862, null)) {
                        object5 = null;
                    } else {
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object7 = G__140862;
                        G__140862 = null;
                        object5 = iLookupThunk.get(object7);
                        if (iLookupThunk == object5) {
                            __thunk__0__ = __site__0__.fault(object7);
                            object5 = G__14086 = __thunk__0__.get(object7);
                        }
                    }
                    if (Util.identical(G__14086, null)) {
                        object4 = null;
                    } else {
                        Object object8 = G__14086;
                        G__14086 = null;
                        object4 = ((IFn)const__7.getRawRoot()).invoke(object8);
                    }
                    Object object9 = temp__5457__auto__14089 = object4;
                    if (object9 != null && object9 != Boolean.FALSE) {
                        Object pred2;
                        Object object10 = temp__5457__auto__14089;
                        temp__5457__auto__14089 = null;
                        Object object11 = pred2 = object10;
                        pred2 = null;
                        Number number = Numbers.num((long)((IDatum)d).getE());
                        Object object12 = d;
                        d = null;
                        ((IFn)object11).invoke((Object)number, ((IDatum)object12).getV(), idmap);
                    }
                }
                Object object13 = seq_14082;
                seq_14082 = null;
                Object object14 = chunk_14083;
                chunk_14083 = null;
                ++i_14085;
                chunk_14083 = object14;
                seq_14082 = object13;
                continue;
            }
            Object object15 = seq_14082;
            seq_14082 = null;
            Object object16 = temp__5457__auto__14092 = ((IFn)const__0.getRawRoot()).invoke(object15);
            if (object16 == null || object16 == Boolean.FALSE) break;
            Object object17 = temp__5457__auto__14092;
            temp__5457__auto__14092 = null;
            Object seq_140822 = object17;
            Object object18 = ((IFn)const__9.getRawRoot()).invoke(seq_140822);
            if (object18 != null && object18 != Boolean.FALSE) {
                Object c__5719__auto__14090 = ((IFn)const__10.getRawRoot()).invoke(seq_140822);
                Object object19 = seq_140822;
                seq_140822 = null;
                Object object20 = c__5719__auto__14090;
                Object object21 = c__5719__auto__14090;
                c__5719__auto__14090 = null;
                i_14085 = (int)0L;
                count_14084 = RT.count((Object)object21);
                chunk_14083 = object20;
                seq_14082 = ((IFn)const__11.getRawRoot()).invoke(object19);
                continue;
            }
            Object d = ((IFn)const__14.getRawRoot()).invoke(seq_140822);
            Object object22 = ((IFn)const__3.getRawRoot()).invoke(db_before, d);
            if (object22 != null && object22 != Boolean.FALSE) {
                Number number = Numbers.num((long)((IDatum)d).getE());
                Object object23 = d;
                d = null;
                ((IFn)const__4.getRawRoot()).invoke(db_before, db_after, (Object)number, ((IDatum)object23).getV(), idmap);
            } else if (((IDatum)d).isAssertion()) {
                Object temp__5457__auto__14091;
                Object object24;
                Object G__14087;
                Object object25;
                Object attr;
                Object object26 = attr = ((IDbImpl)db_before).elementAt(((IDatum)d).getA());
                attr = null;
                Object G__140872 = object26;
                if (Util.identical((Object)G__140872, null)) {
                    object25 = null;
                } else {
                    ILookupThunk iLookupThunk = __thunk__1__;
                    Object object27 = G__140872;
                    G__140872 = null;
                    object25 = iLookupThunk.get(object27);
                    if (iLookupThunk == object25) {
                        __thunk__1__ = __site__1__.fault(object27);
                        object25 = G__14087 = __thunk__1__.get(object27);
                    }
                }
                if (Util.identical(G__14087, null)) {
                    object24 = null;
                } else {
                    Object object28 = G__14087;
                    G__14087 = null;
                    object24 = ((IFn)const__7.getRawRoot()).invoke(object28);
                }
                Object object29 = temp__5457__auto__14091 = object24;
                if (object29 != null && object29 != Boolean.FALSE) {
                    Object pred3;
                    Object object30 = temp__5457__auto__14091;
                    temp__5457__auto__14091 = null;
                    Object object31 = pred3 = object30;
                    pred3 = null;
                    Number number = Numbers.num((long)((IDatum)d).getE());
                    Object object32 = d;
                    d = null;
                    ((IFn)object31).invoke((Object)number, ((IDatum)object32).getV(), idmap);
                }
            }
            Object object33 = seq_140822;
            seq_140822 = null;
            i_14085 = 0L;
            count_14084 = 0L;
            chunk_14083 = null;
            seq_14082 = ((IFn)const__15.getRawRoot()).invoke(object33);
        }
        return null;
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
        return db$ensure_tx.invokeStatic(object5, object6, object7, object8);
    }
}


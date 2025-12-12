/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Indexed
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.Indexed;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$generate_composites$fn__13958;
import datomic.db.EAOpof;
import datomic.impl.db.IDatum;
import java.util.HashMap;
import java.util.HashSet;

public final class db$generate_composites
extends AFunction {
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"long-add!");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"keep");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"constituents"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"comp-ct"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"comp-tx-ms"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object datoms2, Object tx_stat_registers) {
        HashMap<EAOpof, Object> eaop_map = new HashMap<EAOpof, Object>();
        HashSet<IPersistentVector> needed_eas = new HashSet<IPersistentVector>();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object constituents = object2;
        long n__5742__auto__13964 = RT.count((Object)datoms2);
        for (long i = 0L; i < n__5742__auto__13964; ++i) {
            Object composites;
            Object temp__5457__auto__13963;
            Object d = RT.nth((Object)datoms2, (int)RT.uncheckedIntCast((long)i));
            eaop_map.put(new EAOpof(d), d);
            Object object3 = temp__5457__auto__13963 = RT.get((Object)constituents, (Object)((IDatum)d).getA());
            if (object3 == null || object3 == Boolean.FALSE) continue;
            Object object4 = temp__5457__auto__13963;
            temp__5457__auto__13963 = null;
            Object object5 = composites = object4;
            composites = null;
            Object seq_13954 = ((IFn)const__7.getRawRoot()).invoke(object5);
            Object chunk_13955 = null;
            long count_13956 = 0L;
            long i_13957 = 0L;
            while (true) {
                Object c;
                Object temp__5457__auto__13962;
                if (i_13957 < count_13956) {
                    Object c2;
                    Object object6 = c2 = ((Indexed)chunk_13955).nth(RT.uncheckedIntCast((long)i_13957));
                    c2 = null;
                    Boolean bl = needed_eas.add(Tuple.create((Object)Numbers.num((long)((IDatum)d).getE()), (Object)object6)) ? Boolean.TRUE : Boolean.FALSE;
                    Object object7 = seq_13954;
                    seq_13954 = null;
                    Object object8 = chunk_13955;
                    chunk_13955 = null;
                    ++i_13957;
                    chunk_13955 = object8;
                    seq_13954 = object7;
                    continue;
                }
                Object object9 = seq_13954;
                seq_13954 = null;
                Object object10 = temp__5457__auto__13962 = ((IFn)const__7.getRawRoot()).invoke(object9);
                if (object10 == null || object10 == Boolean.FALSE) break;
                Object object11 = temp__5457__auto__13962;
                temp__5457__auto__13962 = null;
                Object seq_139542 = object11;
                Object object12 = ((IFn)const__9.getRawRoot()).invoke(seq_139542);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object c__5719__auto__13961 = ((IFn)const__10.getRawRoot()).invoke(seq_139542);
                    Object object13 = seq_139542;
                    seq_139542 = null;
                    Object object14 = c__5719__auto__13961;
                    Object object15 = c__5719__auto__13961;
                    c__5719__auto__13961 = null;
                    i_13957 = (int)0L;
                    count_13956 = RT.count((Object)object15);
                    chunk_13955 = object14;
                    seq_13954 = ((IFn)const__11.getRawRoot()).invoke(object13);
                    continue;
                }
                Object object16 = c = ((IFn)const__13.getRawRoot()).invoke(seq_139542);
                c = null;
                Boolean bl = needed_eas.add(Tuple.create((Object)Numbers.num((long)((IDatum)d).getE()), (Object)object16)) ? Boolean.TRUE : Boolean.FALSE;
                Object object17 = seq_139542;
                seq_139542 = null;
                i_13957 = 0L;
                count_13956 = 0L;
                chunk_13955 = null;
                seq_13954 = ((IFn)const__14.getRawRoot()).invoke(object17);
            }
        }
        IFn.OLO oLO = (IFn.OLO)const__15.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object18 = tx_stat_registers;
        Object object19 = iLookupThunk2.get(object18);
        if (iLookupThunk2 == object19) {
            __thunk__1__ = __site__1__.fault(object18);
            object19 = __thunk__1__.get(object18);
        }
        oLO.invokePrim(object19, (long)RT.count(needed_eas));
        long start__13414__auto__13965 = System.nanoTime();
        HashMap<EAOpof, Object> hashMap = eaop_map;
        eaop_map = null;
        Object object20 = db2;
        db2 = null;
        HashSet<IPersistentVector> hashSet = needed_eas;
        needed_eas = null;
        Object ret__13415__auto__13966 = ((IFn)const__17.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__18.getRawRoot()).invoke((Object)new db$generate_composites$fn__13958(hashMap, object20)), hashSet);
        IFn.OLO oLO2 = (IFn.OLO)const__15.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object21 = tx_stat_registers;
        tx_stat_registers = null;
        Object object22 = iLookupThunk3.get(object21);
        if (iLookupThunk3 == object22) {
            __thunk__2__ = __site__2__.fault(object21);
            object22 = __thunk__2__.get(object21);
        }
        oLO2.invokePrim(object22, System.nanoTime() - start__13414__auto__13965);
        Object object23 = ret__13415__auto__13966;
        ret__13415__auto__13966 = null;
        return object23;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$generate_composites.invokeStatic(object4, object5, object6);
    }
}


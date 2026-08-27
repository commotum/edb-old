/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query.support$counted_seq$fn__19088;
import datomic.query.support$counted_seq$fn__19090;
import datomic.query.support$counted_seq$fn__19092;
import datomic.query.support$counted_seq$fn__19094;
import datomic.query.support$counted_seq$fn__19096;
import datomic.query.support$counted_seq$fn__19098;
import datomic.query.support$counted_seq$fn__19100;
import datomic.query.support$counted_seq$fn__19102;
import datomic.query.support$counted_seq$fn__19104;
import datomic.query.support$counted_seq$fn__19106;
import datomic.query.support$counted_seq$fn__19108;
import datomic.query.support$counted_seq$fn__19110;
import datomic.query.support$counted_seq$fn__19112;
import datomic.query.support$counted_seq$fn__19114;
import datomic.query.support$counted_seq$fn__19116;
import datomic.query.support$counted_seq$fn__19118;
import datomic.query.support$counted_seq$fn__19120;
import datomic.query.support$counted_seq$fn__19122;
import datomic.query.support$counted_seq$fn__19124;
import datomic.query.support$counted_seq$fn__19126;
import datomic.query.support.proxy$clojure.lang.ASeq$Counted$7e5d62ee;

public final class support$counted_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query.support", (String)"counted-seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"init-proxy");

    public static Object invokeStatic(Object base_seq, Object ct, Object meta) {
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee;
        if (Numbers.lt((Object)ct, (long)1L)) {
            aSeq$Counted$7e5d62ee = null;
        } else {
            Object object = meta;
            meta = null;
            ASeq$Counted$7e5d62ee p__6882__auto__19129 = new ASeq$Counted$7e5d62ee((IPersistentMap)object);
            Object[] objectArray = new Object[40];
            objectArray[0] = "more";
            objectArray[1] = new support$counted_seq$fn__19088(base_seq);
            objectArray[2] = "seq";
            objectArray[3] = new support$counted_seq$fn__19090(base_seq);
            objectArray[4] = "next";
            objectArray[5] = new support$counted_seq$fn__19092(base_seq);
            objectArray[6] = "contains";
            objectArray[7] = new support$counted_seq$fn__19094(base_seq);
            objectArray[8] = "count";
            objectArray[9] = new support$counted_seq$fn__19096(ct);
            objectArray[10] = "listIterator";
            objectArray[11] = new support$counted_seq$fn__19098(base_seq);
            objectArray[12] = "cons";
            objectArray[13] = new support$counted_seq$fn__19100(base_seq);
            objectArray[14] = "iterator";
            objectArray[15] = new support$counted_seq$fn__19102(base_seq);
            objectArray[16] = "subList";
            objectArray[17] = new support$counted_seq$fn__19104(base_seq);
            objectArray[18] = "lastIndexOf";
            objectArray[19] = new support$counted_seq$fn__19106(base_seq);
            objectArray[20] = "withMeta";
            Object object2 = ct;
            ct = null;
            objectArray[21] = new support$counted_seq$fn__19108(object2, base_seq);
            objectArray[22] = "hashCode";
            objectArray[23] = new support$counted_seq$fn__19110(base_seq);
            objectArray[24] = "hasheq";
            objectArray[25] = new support$counted_seq$fn__19112(base_seq);
            objectArray[26] = "indexOf";
            objectArray[27] = new support$counted_seq$fn__19114(base_seq);
            objectArray[28] = "toArray";
            objectArray[29] = new support$counted_seq$fn__19116(base_seq);
            objectArray[30] = "get";
            objectArray[31] = new support$counted_seq$fn__19118(base_seq);
            objectArray[32] = "equals";
            objectArray[33] = new support$counted_seq$fn__19120(base_seq);
            objectArray[34] = "equiv";
            objectArray[35] = new support$counted_seq$fn__19122(base_seq);
            objectArray[36] = "containsAll";
            objectArray[37] = new support$counted_seq$fn__19124(base_seq);
            objectArray[38] = "first";
            Object object3 = base_seq;
            base_seq = null;
            objectArray[39] = new support$counted_seq$fn__19126(object3);
            ((IFn)const__3.getRawRoot()).invoke((Object)p__6882__auto__19129, (Object)RT.mapUniqueKeys((Object[])objectArray));
            aSeq$Counted$7e5d62ee = p__6882__auto__19129;
            Object var3_3 = null;
        }
        return aSeq$Counted$7e5d62ee;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return support$counted_seq.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object base_seq, Object ct) {
        Object object = base_seq;
        base_seq = null;
        Object object2 = ct;
        ct = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return support$counted_seq.invokeStatic(object3, object4);
    }
}


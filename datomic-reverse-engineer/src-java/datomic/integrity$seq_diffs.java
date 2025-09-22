/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$seq_diffs$fn__22457;

public final class integrity$seq_diffs
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"seq-diffs");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object colla, Object collb, Object progress, Object n) {
        LazySeq lazySeq;
        block4: {
            block3: {
                Object n2;
                Object moreb;
                Object b;
                Object morea;
                Object a;
                Object G__22444;
                Object vec__22448;
                Object G__22443;
                Object vec__22445;
                Object object = colla;
                colla = null;
                Object object2 = vec__22445 = (G__22443 = ((IFn)const__2.getRawRoot()).invoke(object));
                vec__22445 = null;
                Object seq__22446 = ((IFn)const__2.getRawRoot()).invoke(object2);
                Object first__22447 = ((IFn)const__3.getRawRoot()).invoke(seq__22446);
                Object object3 = seq__22446;
                seq__22446 = null;
                Object seq__224462 = ((IFn)const__4.getRawRoot()).invoke(object3);
                first__22447 = null;
                seq__224462 = null;
                Object object4 = collb;
                collb = null;
                Object object5 = vec__22448 = (G__22444 = ((IFn)const__2.getRawRoot()).invoke(object4));
                vec__22448 = null;
                Object seq__22449 = ((IFn)const__2.getRawRoot()).invoke(object5);
                Object first__22450 = ((IFn)const__3.getRawRoot()).invoke(seq__22449);
                Object object6 = seq__22449;
                seq__22449 = null;
                Object seq__224492 = ((IFn)const__4.getRawRoot()).invoke(object6);
                first__22450 = null;
                seq__224492 = null;
                Object object7 = n;
                n = null;
                Object n3 = object7;
                Object object8 = G__22443;
                G__22443 = null;
                Object G__224432 = object8;
                Object object9 = G__22444;
                G__22444 = null;
                Object G__224442 = object9;
                Object object10 = n3;
                n3 = null;
                Object n4 = object10;
                while (true) {
                    Object object11;
                    Object or__5238__auto__22460;
                    Object vec__22454;
                    Object vec__22451;
                    Object object12 = G__224432;
                    G__224432 = null;
                    Object object13 = vec__22451 = object12;
                    vec__22451 = null;
                    Object seq__22452 = ((IFn)const__2.getRawRoot()).invoke(object13);
                    Object first__22453 = ((IFn)const__3.getRawRoot()).invoke(seq__22452);
                    Object object14 = seq__22452;
                    seq__22452 = null;
                    Object seq__224522 = ((IFn)const__4.getRawRoot()).invoke(object14);
                    Object object15 = first__22453;
                    first__22453 = null;
                    a = object15;
                    Object object16 = seq__224522;
                    seq__224522 = null;
                    morea = object16;
                    Object object17 = G__224442;
                    G__224442 = null;
                    Object object18 = vec__22454 = object17;
                    vec__22454 = null;
                    Object seq__22455 = ((IFn)const__2.getRawRoot()).invoke(object18);
                    Object first__22456 = ((IFn)const__3.getRawRoot()).invoke(seq__22455);
                    Object object19 = seq__22455;
                    seq__22455 = null;
                    Object seq__224552 = ((IFn)const__4.getRawRoot()).invoke(object19);
                    Object object20 = first__22456;
                    first__22456 = null;
                    b = object20;
                    Object object21 = seq__224552;
                    seq__224552 = null;
                    moreb = object21;
                    Object object22 = n4;
                    n4 = null;
                    n2 = object22;
                    ((IFn)progress).invoke(n2);
                    Object object23 = or__5238__auto__22460 = a;
                    if (object23 != null && object23 != Boolean.FALSE) {
                        object11 = or__5238__auto__22460;
                        or__5238__auto__22460 = null;
                    } else {
                        object11 = b;
                    }
                    if (object11 == null || object11 == Boolean.FALSE) break block3;
                    if (!Util.equiv((Object)a, (Object)b)) break;
                    Object object24 = morea;
                    morea = null;
                    Object object25 = moreb;
                    moreb = null;
                    Object object26 = n2;
                    n2 = null;
                    n4 = Numbers.inc((Object)object26);
                    G__224442 = object25;
                    G__224432 = object24;
                }
                n2 = null;
                morea = null;
                b = null;
                a = null;
                moreb = null;
                lazySeq = new LazySeq((IFn)new integrity$seq_diffs$fn__22457(n2, morea, b, a, moreb, progress));
                break block4;
            }
            lazySeq = null;
        }
        return lazySeq;
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
        return integrity$seq_diffs.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object colla, Object collb, Object progress) {
        Object object = colla;
        colla = null;
        Object object2 = collb;
        collb = null;
        Object object3 = progress;
        progress = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, const__1);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$seq_diffs.invokeStatic(object4, object5, object6);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class cli$cli__GT_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"re-matches");
    public static final Object const__4 = Pattern.compile("-+(.*)");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object strings, Object positions, Object vararg) {
        Object object;
        block4: {
            PersistentArrayMap m;
            Object G__20698;
            Object vec__20699;
            Object object2 = strings;
            strings = null;
            Object object3 = vec__20699 = (G__20698 = object2);
            vec__20699 = null;
            Object seq__20700 = ((IFn)const__0.getRawRoot()).invoke(object3);
            Object first__20701 = ((IFn)const__1.getRawRoot()).invoke(seq__20700);
            Object object4 = seq__20700;
            seq__20700 = null;
            Object seq__207002 = ((IFn)const__2.getRawRoot()).invoke(object4);
            first__20701 = null;
            seq__207002 = null;
            Object object5 = positions;
            positions = null;
            Object positions2 = object5;
            PersistentArrayMap m2 = PersistentArrayMap.EMPTY;
            Object object6 = G__20698;
            G__20698 = null;
            Object G__206982 = object6;
            Object object7 = positions2;
            positions2 = null;
            Object positions3 = object7;
            PersistentArrayMap persistentArrayMap = m2;
            m2 = null;
            Object m3 = persistentArrayMap;
            while (true) {
                Object vec__20711;
                Object temp__5455__auto__20715;
                Object vec__20702;
                Object object8 = G__206982;
                G__206982 = null;
                Object object9 = vec__20702 = object8;
                vec__20702 = null;
                Object seq__20703 = ((IFn)const__0.getRawRoot()).invoke(object9);
                Object first__20704 = ((IFn)const__1.getRawRoot()).invoke(seq__20703);
                Object object10 = seq__20703;
                seq__20703 = null;
                Object seq__207032 = ((IFn)const__2.getRawRoot()).invoke(object10);
                Object object11 = first__20704;
                first__20704 = null;
                Object s = object11;
                Object object12 = seq__207032;
                seq__207032 = null;
                Object more = object12;
                Object object13 = positions3;
                positions3 = null;
                Object positions4 = object13;
                PersistentArrayMap persistentArrayMap2 = m3;
                m3 = null;
                m = persistentArrayMap2;
                Object object14 = s;
                if (object14 == null || object14 == Boolean.FALSE) break;
                Object object15 = temp__5455__auto__20715 = ((IFn)const__3.getRawRoot()).invoke(const__4, s);
                if (object15 != null && object15 != Boolean.FALSE) {
                    Object more2;
                    Object vec__20708;
                    Object object16 = temp__5455__auto__20715;
                    temp__5455__auto__20715 = null;
                    Object vec__20705 = object16;
                    RT.nth((Object)vec__20705, (int)RT.intCast((long)0L), null);
                    Object object17 = vec__20705;
                    vec__20705 = null;
                    Object k = RT.nth((Object)object17, (int)RT.intCast((long)1L), null);
                    if (Util.equiv((Object)((IFn)const__9.getRawRoot()).invoke(k), (Object)vararg)) {
                        PersistentArrayMap persistentArrayMap3 = m;
                        m = null;
                        Object object18 = k;
                        k = null;
                        Object object19 = s;
                        s = null;
                        Object object20 = more;
                        more = null;
                        object = ((IFn)const__10.getRawRoot()).invoke((Object)persistentArrayMap3, ((IFn)const__9.getRawRoot()).invoke(object18), ((IFn)const__11.getRawRoot()).invoke((Object)Tuple.create((Object)object19), object20));
                        break block4;
                    }
                    Object object21 = more;
                    more = null;
                    Object object22 = vec__20708 = object21;
                    vec__20708 = null;
                    Object seq__20709 = ((IFn)const__0.getRawRoot()).invoke(object22);
                    Object first__20710 = ((IFn)const__1.getRawRoot()).invoke(seq__20709);
                    Object object23 = seq__20709;
                    seq__20709 = null;
                    Object seq__207092 = ((IFn)const__2.getRawRoot()).invoke(object23);
                    Object object24 = first__20710;
                    first__20710 = null;
                    Object v = object24;
                    Object object25 = seq__207092;
                    seq__207092 = null;
                    Object object26 = more2 = object25;
                    more2 = null;
                    Object object27 = positions4;
                    positions4 = null;
                    PersistentArrayMap persistentArrayMap4 = m;
                    m = null;
                    Object object28 = k;
                    k = null;
                    Object object29 = v;
                    v = null;
                    m3 = ((IFn)const__10.getRawRoot()).invoke((Object)persistentArrayMap4, ((IFn)const__9.getRawRoot()).invoke(object28), object29);
                    positions3 = object27;
                    G__206982 = object26;
                    continue;
                }
                Object object30 = positions4;
                positions4 = null;
                Object object31 = vec__20711 = object30;
                vec__20711 = null;
                Object seq__20712 = ((IFn)const__0.getRawRoot()).invoke(object31);
                Object first__20713 = ((IFn)const__1.getRawRoot()).invoke(seq__20712);
                Object object32 = seq__20712;
                seq__20712 = null;
                Object seq__207122 = ((IFn)const__2.getRawRoot()).invoke(object32);
                Object object33 = first__20713;
                first__20713 = null;
                Object k = object33;
                Object object34 = seq__207122;
                seq__207122 = null;
                Object pmore = object34;
                if (Util.equiv((Object)((IFn)const__9.getRawRoot()).invoke(k), (Object)vararg)) {
                    PersistentArrayMap persistentArrayMap5 = m;
                    m = null;
                    Object object35 = k;
                    k = null;
                    Object object36 = s;
                    s = null;
                    Object object37 = more;
                    more = null;
                    object = ((IFn)const__10.getRawRoot()).invoke((Object)persistentArrayMap5, ((IFn)const__9.getRawRoot()).invoke(object35), ((IFn)const__11.getRawRoot()).invoke((Object)Tuple.create((Object)object36), object37));
                    break block4;
                }
                Object object38 = more;
                more = null;
                Object object39 = pmore;
                pmore = null;
                PersistentArrayMap persistentArrayMap6 = m;
                m = null;
                Object object40 = k;
                k = null;
                Object object41 = s;
                s = null;
                m3 = ((IFn)const__10.getRawRoot()).invoke((Object)persistentArrayMap6, ((IFn)const__9.getRawRoot()).invoke(object40), object41);
                positions3 = object39;
                G__206982 = object38;
            }
            object = m;
            m = null;
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
        return cli$cli__GT_map.invokeStatic(object4, object5, object6);
    }
}


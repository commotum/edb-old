/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.HashSet;

public final class query$rename_self_unifications$fn__19400$fn__19401
extends AFunction {
    Object padding;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"=");

    public query$rename_self_unifications$fn__19400$fn__19401(Object object) {
        this.padding = object;
    }

    public Object invoke(Object result2, Object clause) {
        PersistentVector rclause;
        Object result3;
        Object G__19405;
        Object vec__19406;
        Object object = result2;
        result2 = null;
        Object result4 = object;
        HashSet<Object> syms = new HashSet<Object>();
        PersistentVector rclause2 = PersistentVector.EMPTY;
        Object object2 = clause;
        clause = null;
        Object object3 = vec__19406 = (G__19405 = object2);
        vec__19406 = null;
        Object seq__19407 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object first__19408 = ((IFn)const__1.getRawRoot()).invoke(seq__19407);
        Object object4 = seq__19407;
        seq__19407 = null;
        Object seq__194072 = ((IFn)const__2.getRawRoot()).invoke(object4);
        first__19408 = null;
        seq__194072 = null;
        Object object5 = result4;
        result4 = null;
        Object result5 = object5;
        HashSet<Object> hashSet = syms;
        syms = null;
        HashSet<Object> syms2 = hashSet;
        PersistentVector persistentVector = rclause2;
        rclause2 = null;
        Object rclause3 = persistentVector;
        Object object6 = G__19405;
        G__19405 = null;
        Object G__194052 = object6;
        while (true) {
            Object vec__19409;
            Object object7 = result5;
            result5 = null;
            result3 = object7;
            HashSet<Object> hashSet2 = syms2;
            syms2 = null;
            HashSet<Object> syms3 = hashSet2;
            PersistentVector persistentVector2 = rclause3;
            rclause3 = null;
            rclause = persistentVector2;
            Object object8 = G__194052;
            G__194052 = null;
            Object object9 = vec__19409 = object8;
            vec__19409 = null;
            Object seq__19410 = ((IFn)const__0.getRawRoot()).invoke(object9);
            Object first__19411 = ((IFn)const__1.getRawRoot()).invoke(seq__19410);
            Object object10 = seq__19410;
            seq__19410 = null;
            Object seq__194102 = ((IFn)const__2.getRawRoot()).invoke(object10);
            Object object11 = first__19411;
            first__19411 = null;
            Object item = object11;
            Object object12 = seq__194102;
            seq__194102 = null;
            Object more = object12;
            Object object13 = item;
            if (object13 == null || object13 == Boolean.FALSE) break;
            Object object14 = ((IFn)const__3.getRawRoot()).invoke(item);
            if (object14 != null && object14 != Boolean.FALSE) {
                if (syms3.add(item)) {
                    Object object15 = result3;
                    result3 = null;
                    HashSet<Object> hashSet3 = syms3;
                    syms3 = null;
                    PersistentVector persistentVector3 = rclause;
                    rclause = null;
                    Object object16 = item;
                    item = null;
                    Object object17 = more;
                    more = null;
                    G__194052 = object17;
                    rclause3 = ((IFn)const__4.getRawRoot()).invoke((Object)persistentVector3, object16);
                    syms2 = hashSet3;
                    result5 = object15;
                    continue;
                }
                Object rsym = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(item, this_.padding));
                Object object18 = result3;
                result3 = null;
                Object object19 = item;
                item = null;
                Object object20 = ((IFn)const__4.getRawRoot()).invoke(object18, ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)const__11), ((IFn)const__10.getRawRoot()).invoke(object19), ((IFn)const__10.getRawRoot()).invoke(rsym))))))));
                HashSet<Object> hashSet4 = syms3;
                syms3 = null;
                PersistentVector persistentVector4 = rclause;
                rclause = null;
                Object object21 = rsym;
                rsym = null;
                Object object22 = more;
                more = null;
                G__194052 = object22;
                rclause3 = ((IFn)const__4.getRawRoot()).invoke((Object)persistentVector4, object21);
                syms2 = hashSet4;
                result5 = object20;
                continue;
            }
            Object object23 = result3;
            result3 = null;
            HashSet<Object> hashSet5 = syms3;
            syms3 = null;
            PersistentVector persistentVector5 = rclause;
            rclause = null;
            Object object24 = item;
            item = null;
            Object object25 = more;
            more = null;
            G__194052 = object25;
            rclause3 = ((IFn)const__4.getRawRoot()).invoke((Object)persistentVector5, object24);
            syms2 = hashSet5;
            result5 = object23;
        }
        Object object26 = result3;
        result3 = null;
        PersistentVector persistentVector6 = rclause;
        rclause = null;
        query$rename_self_unifications$fn__19400$fn__19401 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object26, (Object)persistentVector6);
    }
}


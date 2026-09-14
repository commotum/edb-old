/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class common$coll_compare
extends AFunction
implements IFn.OOL {
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"cl");
    public static final Object const__3 = -1L;
    public static final Object const__4 = 1L;
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"cc");

    public static long invokeStatic(Object a, Object b) {
        Object object;
        if (a instanceof List) {
            if (b instanceof List) {
                Object object2 = a;
                a = null;
                Object object3 = b;
                b = null;
                object = Numbers.num((long)((IFn.OOL)const__2.getRawRoot()).invokePrim(object2, object3));
            } else {
                object = const__3;
            }
        } else if (b instanceof List) {
            object = const__4;
        } else if (a instanceof Map) {
            if (b instanceof Map) {
                Object object4 = a;
                a = null;
                Object object5 = b;
                b = null;
                object = Numbers.num((long)((IFn.OOL)const__6.getRawRoot()).invokePrim(object4, object5));
            } else {
                object = const__3;
            }
        } else if (b instanceof Map) {
            object = const__4;
        } else if (a instanceof Set) {
            if (b instanceof Set) {
                Object object6 = a;
                a = null;
                Object object7 = b;
                b = null;
                object = Numbers.num((long)((IFn.OOL)const__6.getRawRoot()).invokePrim(object6, object7));
            } else {
                object = const__3;
            }
        } else {
            object = null;
        }
        return ((Number)object).longValue();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$coll_compare.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$coll_compare.invokeStatic(object3, object4);
    }
}


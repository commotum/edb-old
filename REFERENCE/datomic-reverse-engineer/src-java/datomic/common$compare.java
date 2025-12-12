/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
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

public final class common$compare
extends AFunction
implements IFn.OOL {
    public static final Object const__1 = 0L;
    public static final Object const__3 = -1L;
    public static final Object const__4 = 1L;
    public static final Keyword const__8 = RT.keyword(null, (String)"else");
    public static final Var const__9 = RT.var((String)"datomic.common", (String)"compare-ex");

    /*
     * WARNING - void declaration
     */
    public static long invokeStatic(Object a, Object b) {
        Object object;
        if (Util.identical((Object)a, (Object)b)) {
            object = const__1;
        } else if (Util.identical((Object)a, null)) {
            Object object2 = b;
            b = null;
            object = Util.identical((Object)object2, null) ? const__1 : const__3;
        } else if (Util.identical((Object)b, null)) {
            object = const__4;
        } else if (a instanceof Number) {
            if (b instanceof Number) {
                Object object3 = a;
                a = null;
                Object object4 = b;
                b = null;
                object = Numbers.compare((Number)((Number)object3), (Number)((Number)object4));
            } else {
                object = const__3;
            }
        } else {
            void var2_2;
            boolean and__5236__auto__9020 = a instanceof String;
            if (and__5236__auto__9020 ? b instanceof String : var2_2) {
                Object object5 = a;
                a = null;
                Object object6 = b;
                b = null;
                object = ((Comparable)object5).compareTo(object6);
            } else if (b instanceof Number) {
                object = const__4;
            } else {
                Keyword keyword = const__8;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object7 = a;
                    a = null;
                    Object object8 = b;
                    b = null;
                    object = Numbers.num((long)((IFn.OOL)const__9.getRawRoot()).invokePrim(object7, object8));
                } else {
                    object = null;
                }
            }
        }
        return ((Number)object).longValue();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$compare.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$compare.invokeStatic(object3, object4);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$cc
extends AFunction
implements IFn.OOL {
    public static final Object const__2 = -1L;
    public static final Object const__4 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"else");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"cx");

    public static long invokeStatic(Object a, Object b) {
        Object object;
        int cb;
        int ca = RT.count((Object)a);
        if ((long)ca < (long)(cb = RT.count((Object)b))) {
            object = const__2;
        } else if ((long)ca > (long)cb) {
            object = const__4;
        } else {
            Keyword keyword = const__5;
            if (keyword != null && keyword != Boolean.FALSE) {
                int hb;
                int ha = a.hashCode();
                if ((long)ha == (long)(hb = b.hashCode())) {
                    Object object2 = a;
                    a = null;
                    Object object3 = b;
                    b = null;
                    object = Numbers.num((long)((IFn.OOL)const__7.getRawRoot()).invokePrim(object2, object3));
                } else {
                    object = Numbers.num((long)Numbers.unchecked_minus((long)ha, (long)hb));
                }
            } else {
                object = null;
            }
        }
        return ((Number)object).longValue();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$cc.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$cc.invokeStatic(object3, object4);
    }
}


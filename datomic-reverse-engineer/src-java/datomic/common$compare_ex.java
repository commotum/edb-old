/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IRecord
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IRecord;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class common$compare_ex
extends AFunction
implements IFn.OOL {
    public static final Object const__0 = 0L;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"instance?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"coll-compare");
    public static final Var const__10 = RT.var((String)"datomic.common", (String)"BYTES");
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"compare-byte-arrays");
    public static final Object const__13 = 1L;
    public static final Keyword const__14 = RT.keyword(null, (String)"else");

    public static long invokeStatic(Object a, Object b) {
        Object object;
        if (a.equals(b)) {
            object = const__0;
        } else {
            Boolean bl;
            boolean or__5238__auto__9017 = a instanceof List;
            if (or__5238__auto__9017) {
                bl = or__5238__auto__9017 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Boolean or__5238__auto__9016;
                boolean and__5236__auto__9015 = a instanceof Map;
                Boolean bl2 = or__5238__auto__9016 = and__5236__auto__9015 ? ((IFn)const__4.getRawRoot()).invoke((Object)(a instanceof IRecord ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__9015 ? Boolean.TRUE : Boolean.FALSE);
                if (bl2 != null && bl2 != Boolean.FALSE) {
                    bl = or__5238__auto__9016;
                    or__5238__auto__9016 = null;
                } else {
                    bl = a instanceof Set ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            if (bl != null && bl != Boolean.FALSE) {
                Object object2 = a;
                a = null;
                Object object3 = b;
                b = null;
                object = Numbers.num((long)RT.longCast((Object)((IFn)const__8.getRawRoot()).invoke(object2, object3)));
            } else if (Util.identical(a.getClass(), b.getClass())) {
                Object object4 = ((IFn)const__1.getRawRoot()).invoke(const__10.getRawRoot(), a);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object object5 = a;
                    a = null;
                    Object object6 = b;
                    b = null;
                    object = Numbers.num((long)((IFn.OOL)const__11.getRawRoot()).invokePrim(object5, object6));
                } else {
                    Object object7 = a;
                    a = null;
                    Object object8 = b;
                    b = null;
                    object = ((Comparable)object7).compareTo(object8);
                }
            } else {
                boolean or__5238__auto__9018 = b instanceof Collection;
                if (or__5238__auto__9018 ? or__5238__auto__9018 : b instanceof Map) {
                    object = const__13;
                } else {
                    Keyword keyword = const__14;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object9 = a;
                        a = null;
                        Object object10 = b;
                        b = null;
                        object = object9.getClass().getName().compareTo(object10.getClass().getName());
                    } else {
                        object = null;
                    }
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
        return new Long(common$compare_ex.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$compare_ex.invokeStatic(object3, object4);
    }
}


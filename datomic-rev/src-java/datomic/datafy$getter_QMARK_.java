/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$getter_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__2 = 0L;
    public static final Object const__3 = 2L;
    public static final Object const__4 = 3L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not=");

    public static Object invokeStatic(Object m) {
        Object object;
        boolean and__5236__auto__17178;
        String name = ((Method)m).getName();
        boolean or__5238__auto__17176 = Util.equiv((Object)"is", (Object)((IFn)const__1.getRawRoot()).invoke((Object)name, const__2, const__3));
        boolean bl = and__5236__auto__17178 = or__5238__auto__17176 ? or__5238__auto__17176 : Util.equiv((Object)"get", (Object)((IFn)const__1.getRawRoot()).invoke((Object)name, const__2, const__4));
        if (and__5236__auto__17178) {
            Object and__5236__auto__17177;
            String string = name;
            name = null;
            Object object2 = and__5236__auto__17177 = ((IFn)const__5.getRawRoot()).invoke((Object)"getClass", (Object)string);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = m;
                m = null;
                object = Util.equiv((long)0L, (long)RT.count(((Method)object3).getParameterTypes())) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object = and__5236__auto__17177;
                Object var3_3 = null;
            }
        } else {
            object = and__5236__auto__17178 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$getter_QMARK_.invokeStatic(object2);
    }
}


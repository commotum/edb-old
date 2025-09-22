/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$invoke_getter
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__2 = 0L;
    public static final Object const__3 = 3L;
    public static final Object const__4 = 2L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Object const__7 = 1L;
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"clojure.core", (String)"when-let");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"v__17285__auto__");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"symbol");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"object-to-data-wrapper");

    public static Object invokeStatic(Object o, Object m, Object meth) {
        Object object = meth;
        meth = null;
        String rm = ((Method)object).getName();
        Object base = Util.equiv((Object)"get", (Object)((IFn)const__1.getRawRoot()).invoke((Object)rm, const__2, const__3)) ? ((IFn)const__1.getRawRoot()).invoke((Object)rm, const__3) : ((IFn)const__1.getRawRoot()).invoke((Object)rm, const__4);
        String string = ((String)((IFn)const__1.getRawRoot()).invoke(base, const__2, const__7)).toLowerCase();
        Object object2 = base;
        base = null;
        Object kw = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)string, ((IFn)const__1.getRawRoot()).invoke(object2, const__7)));
        String string2 = rm;
        rm = null;
        Object object3 = o;
        o = null;
        Object object4 = kw;
        kw = null;
        return ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)const__11), ((IFn)const__10.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)const__14), ((IFn)const__10.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)".", (Object)string2))), ((IFn)const__10.getRawRoot()).invoke(object3)))))))), ((IFn)const__10.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object4), ((IFn)const__10.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)const__16), ((IFn)const__10.getRawRoot()).invoke((Object)const__14))))))))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datafy$invoke_getter.invokeStatic(object4, object5, object6);
    }
}


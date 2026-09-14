/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datafy$define_object_to_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"getters");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"clojure.core", (String)"extend-protocol");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"ObjectToData");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"object-to-data");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"apply");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"hash-map");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"clojure.core", (String)"concat");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__17 = RT.var((String)"datomic.datafy", (String)"invoke-getter");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"m");

    public static Object invokeStatic(Object bean_class) {
        Object or__5238__auto__17297;
        Object object = or__5238__auto__17297 = ((IFn)const__1.getRawRoot()).invoke(bean_class);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__2.getRawRoot()).invoke((Object)"Unable to resolve symbol as class: ", bean_class));
        }
        Object object2 = or__5238__auto__17297;
        or__5238__auto__17297 = null;
        Object g = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = bean_class;
        bean_class = null;
        Object object4 = g;
        g = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), ((IFn)const__5.getRawRoot()).invoke((Object)const__7), ((IFn)const__5.getRawRoot()).invoke(object3), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__8), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__11))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__12), ((IFn)const__5.getRawRoot()).invoke((Object)const__13), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__14), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), (Object)const__18, (Object)const__19), object4))))))))))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$define_object_to_data.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$invoke_setter
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"setter-name->keyword");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"when");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"clojure.core", (String)"contains?");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"v");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"k");
    public static final Keyword const__13 = RT.keyword(null, (String)"tag");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"symbol");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"property-to-object");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"class");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"v");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"k");

    public static Object invokeStatic(Object o, Object m, Object meth) {
        String wm = ((Method)meth).getName();
        Object kw = ((IFn)const__0.getRawRoot()).invoke((Object)wm);
        Object object = meth;
        meth = null;
        Object type = ((IFn)const__1.getRawRoot()).invoke(((Method)object).getParameterTypes());
        Object object2 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__6), ((IFn)const__4.getRawRoot()).invoke(m), ((IFn)const__4.getRawRoot()).invoke(kw))));
        Object object3 = m;
        m = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(kw), ((IFn)const__4.getRawRoot()).invoke(object3))));
        Object[] objectArray = new Object[]{const__13, ((IFn)const__14.getRawRoot()).invoke((Object)((Class)type).getName())};
        Object object5 = kw;
        kw = null;
        Object object6 = type;
        type = null;
        Object object7 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__10), object4, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)const__12, (Object)RT.mapUniqueKeys((Object[])objectArray))), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__15), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__16), ((IFn)const__4.getRawRoot()).invoke(o)))), ((IFn)const__4.getRawRoot()).invoke(object5), ((IFn)const__4.getRawRoot()).invoke((Object)const__17), ((IFn)const__4.getRawRoot()).invoke(object6))))))));
        Object object8 = o;
        o = null;
        String string = wm;
        wm = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__5), object2, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__7), object7, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__18), ((IFn)const__4.getRawRoot()).invoke(object8), ((IFn)const__4.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)string)), ((IFn)const__4.getRawRoot()).invoke((Object)const__19)))))))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datafy$invoke_setter.invokeStatic(object4, object5, object6);
    }
}


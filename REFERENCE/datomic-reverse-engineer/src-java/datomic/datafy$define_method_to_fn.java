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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datafy$define_method_to_fn$fn__17304;
import datomic.datafy$define_method_to_fn$fn__17307;
import java.lang.reflect.Method;

public final class datafy$define_method_to_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map-indexed");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"o");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Keyword const__11 = RT.keyword(null, (String)"ok");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"object-to-data");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"defn");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Keyword const__17 = RT.keyword(null, (String)"related-class");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__20 = RT.var((String)"datomic.datafy", (String)"type-docstring");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"cons");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"o");
    public static final Keyword const__24 = RT.keyword(null, (String)"tag");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object m, Object fname, Object docstring) {
        Object object;
        Object argspecs = ((IFn)const__0.getRawRoot()).invoke((Object)new datafy$define_method_to_fn$fn__17304(), ((Method)m).getParameterTypes());
        Object args = ((IFn)const__1.getRawRoot()).invoke((Object)new datafy$define_method_to_fn$fn__17307(), argspecs);
        if (Util.equiv(Void.TYPE, ((Method)m).getReturnType())) {
            Object object2 = args;
            args = null;
            object = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__8), ((IFn)const__7.getRawRoot()).invoke((Object)const__9), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)((Method)m).getName())), object2))), ((IFn)const__7.getRawRoot()).invoke((Object)const__11))));
        } else {
            Object object3 = args;
            args = null;
            object = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__12), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__13), ((IFn)const__7.getRawRoot()).invoke((Object)const__14), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)((Method)m).getName())), object3)))))))));
        }
        Object invoc = object;
        Object object4 = fname;
        fname = null;
        Object[] objectArray = new Object[]{const__17, ((Method)m).getDeclaringClass()};
        Object object5 = docstring;
        docstring = null;
        Object object6 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)"Datafication of ", ((Method)m).getDeclaringClass(), (Object)".", (Object)((Method)m).getName(), (Object)"\n", (Object)"Generated argument docs:\n\n", ((IFn)const__3.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__19.getRawRoot()).invoke(const__20.getRawRoot(), argspecs)), object5));
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__24;
        Object object7 = m;
        m = null;
        objectArray2[1] = ((IFn)const__10.getRawRoot()).invoke((Object)((Method)object7).getDeclaringClass().getName());
        Object object8 = argspecs;
        argspecs = null;
        Object object9 = invoc;
        invoc = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__15), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray))), object6, ((IFn)const__7.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)const__23, (Object)RT.mapUniqueKeys((Object[])objectArray2)), ((IFn)const__1.getRawRoot()).invoke(const__25.getRawRoot(), object8)))), object9));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datafy$define_method_to_fn.invokeStatic(object4, object5, object6);
    }
}


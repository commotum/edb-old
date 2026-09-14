/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.Exceptions;

public final class error$argd
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"add-details-to-msg");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"anomalize");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword((String)"db", (String)"error");
    public static final Object const__6 = RT.classForName((String)"datomic.impl.Exceptions$IllegalArgumentExceptionInfo");

    public static Object invokeStatic(Object code, Object msg, Object details, Object cause) {
        Object object = msg;
        msg = null;
        Object msg__663__auto__679 = ((IFn)const__1.getRawRoot()).invoke(object, details);
        String string = (String)((IFn)const__2.getRawRoot()).invoke(code, (Object)" ", msg__663__auto__679);
        Object object2 = details;
        details = null;
        Object object3 = code;
        code = null;
        Object object4 = msg__663__auto__679;
        msg__663__auto__679 = null;
        Object object5 = cause;
        cause = null;
        throw (Throwable)new Exceptions.IllegalArgumentExceptionInfo(string, (IPersistentMap)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object2, (Object)const__5, object3), const__6, object4), (Throwable)object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return error$argd.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object code, Object msg, Object details) {
        Object object = msg;
        msg = null;
        Object msg__662__auto__680 = ((IFn)const__1.getRawRoot()).invoke(object, details);
        String string = (String)((IFn)const__2.getRawRoot()).invoke(code, (Object)" ", msg__662__auto__680);
        Object object2 = details;
        details = null;
        Object object3 = code;
        code = null;
        Object object4 = msg__662__auto__680;
        msg__662__auto__680 = null;
        throw (Throwable)new Exceptions.IllegalArgumentExceptionInfo(string, (IPersistentMap)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object2, (Object)const__5, object3), const__6, object4));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return error$argd.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object code, Object msg) {
        Object object = code;
        code = null;
        Object object2 = msg;
        msg = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return error$argd.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object code) {
        Object object = code;
        code = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)"", null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$argd.invokeStatic(object2);
    }
}


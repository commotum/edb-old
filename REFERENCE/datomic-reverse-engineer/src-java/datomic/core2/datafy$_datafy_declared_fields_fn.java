/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Reflector
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Reflector;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.datafy$_datafy_declared_fields_fn$fn__20545;
import datomic.core2.datafy$_datafy_declared_fields_fn$fn__20547;

public final class datafy$_datafy_declared_fields_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"class?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"extend-protocol");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core.protocols", (String)"Datafiable");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"symbol");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"datafy");
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"this"));
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object clsym) {
        Object cls = ((IFn)const__0.getRawRoot()).invoke(clsym);
        Object object = ((IFn)const__1.getRawRoot()).invoke(cls);
        if (object == null || object == Boolean.FALSE) {
            Object object2 = clsym;
            clsym = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__2.getRawRoot()).invoke((Object)"Not a class: [", object2, (Object)"]"));
        }
        Object field_exprs = ((IFn)const__3.getRawRoot()).invoke((Object)new datafy$_datafy_declared_fields_fn$fn__20545(), ((IFn)const__4.getRawRoot()).invoke((Object)new datafy$_datafy_declared_fields_fn$fn__20547(), Reflector.invokeNoArgInstanceMember((Object)cls, (String)"getDeclaredFields", (boolean)false)));
        Object object3 = cls;
        cls = null;
        Object object4 = field_exprs;
        field_exprs = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)const__6, (Object)const__7, ((IFn)const__8.getRawRoot()).invoke(Reflector.invokeNoArgInstanceMember((Object)object3, (String)"getName", (boolean)false)), ((IFn)const__5.getRawRoot()).invoke((Object)const__9, (Object)const__11, ((IFn)const__12.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object4)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$_datafy_declared_fields_fn.invokeStatic(object2);
    }
}


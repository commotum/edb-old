/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class function$compile_clojure
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*ns*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"in-ns");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"use");
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"), (Object)RT.keyword(null, (String)"only"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"q"), Symbol.intern(null, (String)"db")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 38})));
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"eval");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"import");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object imports, Object requires, Object params, Object code) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, const__2.get()));
        try {
            Object object2 = code;
            code = null;
            Object code2 = ((IFn)const__3.getRawRoot()).invoke(object2);
            Object object3 = params;
            params = null;
            Object object4 = code2;
            code2 = null;
            Object expr = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7), ((IFn)const__6.getRawRoot()).invoke(object3), ((IFn)const__6.getRawRoot()).invoke(object4)));
            Object gns = ((IFn)const__8.getRawRoot()).invoke((Object)"ns_");
            ((IFn)const__9.getRawRoot()).invoke(gns);
            ((IFn)const__10.getRawRoot()).invoke((Object)const__11);
            ((IFn)const__12.getRawRoot()).invoke((Object)const__13);
            Object object5 = imports;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = imports;
                imports = null;
                ((IFn)const__14.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__15), object6)));
            }
            Object object7 = ((IFn)const__4.getRawRoot()).invoke(requires);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = requires;
                requires = null;
                ((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), object8);
            }
            Object object9 = expr;
            expr = null;
            Object f = ((IFn)const__14.getRawRoot()).invoke(object9);
            Object object10 = gns;
            gns = null;
            Namespace.remove((Symbol)((Symbol)object10));
            Object object11 = f;
            f = null;
            object = object11;
        }
        finally {
            ((IFn)const__18.getRawRoot()).invoke();
        }
        return object;
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
        return function$compile_clojure.invokeStatic(object5, object6, object7, object8);
    }
}


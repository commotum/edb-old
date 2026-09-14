/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.StringWriter;

public final class datafy$type_docstring
extends AFunction {
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.pprint", (String)"pprint");
    public static final Var const__10 = RT.var((String)"datomic.datafy", (String)"type-descriptor");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object p__17298) {
        Object object;
        Object object2 = p__17298;
        p__17298 = null;
        Object vec__17299 = object2;
        Object argname = RT.nth((Object)vec__17299, (int)RT.intCast((long)0L), null);
        RT.nth((Object)vec__17299, (int)RT.intCast((long)1L), null);
        Object object3 = vec__17299;
        vec__17299 = null;
        Object argclass = RT.nth((Object)object3, (int)RT.intCast((long)2L), null);
        StringWriter s__6071__auto__17303 = new StringWriter();
        ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6, (Object)s__6071__auto__17303));
        try {
            Object object4 = argname;
            argname = null;
            ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)"Shape of ", object4, (Object)":"));
            Object object5 = argclass;
            argclass = null;
            ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object5));
            ((IFn)const__7.getRawRoot()).invoke();
            StringWriter stringWriter = s__6071__auto__17303;
            s__6071__auto__17303 = null;
            object = ((IFn)const__8.getRawRoot()).invoke((Object)stringWriter);
        }
        finally {
            ((IFn)const__11.getRawRoot()).invoke();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$type_docstring.invokeStatic(object2);
    }
}


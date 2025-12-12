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

public final class monitor$load_callback
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"resolve");

    public static Object invokeStatic(Object prop_name) {
        Object object;
        Object temp__5457__auto__568;
        Object object2;
        Object object3 = prop_name;
        prop_name = null;
        String G__565 = System.getProperty((String)object3);
        if (Util.identical((Object)G__565, null)) {
            object2 = null;
        } else {
            String string = G__565;
            G__565 = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke((Object)string);
        }
        Object object4 = temp__5457__auto__568 = object2;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__568;
            temp__5457__auto__568 = null;
            Object s = object5;
            Object object6 = ((IFn)const__2.getRawRoot()).invoke(s);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object temp__5457__auto__567;
                Object object7 = temp__5457__auto__567 = ((IFn)const__3.getRawRoot()).invoke(s);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object ns;
                    Object object8 = temp__5457__auto__567;
                    temp__5457__auto__567 = null;
                    Object object9 = ns = object8;
                    ns = null;
                    ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object9));
                    Object object10 = s;
                    s = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10));
                } else {
                    object = null;
                }
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return monitor$load_callback.invokeStatic(object2);
    }
}


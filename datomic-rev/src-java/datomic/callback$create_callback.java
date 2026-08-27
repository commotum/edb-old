/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class callback$create_callback
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"datomic.callback", (String)"compile-static-method-callback");

    public static Object invokeStatic(Object sym) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(sym);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5455__auto__10101;
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(sym)));
            Object object3 = temp__5455__auto__10101 = ((IFn)const__3.getRawRoot()).invoke(sym);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object cb;
                Object object4 = temp__5455__auto__10101;
                temp__5455__auto__10101 = null;
                object = cb = object4;
                cb = null;
            } else {
                Logger logger = LoggerFactory.getLogger((String)"datomic.callback");
                if (logger.isWarnEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object object5 = sym;
                    sym = null;
                    logger2.warn((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)"Callback ", object5, (Object)" does not exist")));
                }
                object = null;
            }
        } else {
            Object object6 = sym;
            sym = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object6);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return callback$create_callback.invokeStatic(object2);
    }
}


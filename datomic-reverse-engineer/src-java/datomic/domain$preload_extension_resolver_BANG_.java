/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class domain$preload_extension_resolver_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"resource");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"config-resource");
    public static final Var const__2 = RT.var((String)"datomic.extension-resolver", (String)"preload!");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"extension-resolver", (String)"preload!")});

    public static Object invokeStatic() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__2.getRawRoot()).invoke(const__1.getRawRoot());
            Logger logger = LoggerFactory.getLogger((String)"datomic.domain");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__3.getRawRoot()).invoke((Object)const__6));
            }
            v2 = null;
        } else {
            v2 = null;
        }
        return v2;
    }

    public Object invoke() {
        return domain$preload_extension_resolver_BANG_.invokeStatic();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class log$write_excise_val
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"log", (String)"write-excise-val");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__5 = RT.keyword(null, (String)"created");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"zip-and-create");

    public static Object invokeStatic(Object cs, Object uuid, Object fressianed_buf) {
        Logger logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, uuid})));
        }
        Object object = cs;
        cs = null;
        Object object2 = uuid;
        uuid = null;
        Object object3 = fressianed_buf;
        fressianed_buf = null;
        if (!Util.equiv((Object)const__5, (Object)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object, object2, object3)))) {
            throw (Throwable)new Error("Cluster value creation failed. Unable to write log.");
        }
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$write_excise_val.invokeStatic(object4, object5, object6);
    }
}


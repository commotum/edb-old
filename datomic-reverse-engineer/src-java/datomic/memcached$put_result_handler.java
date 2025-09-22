/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class memcached$put_result_handler
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.memcached", (String)"safe-deref");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__2 = RT.keyword(null, (String)"msg");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"ex");

    public static Object invokeStatic(Object fut) {
        Object object;
        try {
            Object object2 = fut;
            fut = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object2);
        }
        catch (Throwable t2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.memcached");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__2;
                objectArray[1] = ((IFn)const__3.getRawRoot()).invoke((Object)"Cache put-result-handler error: ", (Object)t2.getMessage());
                objectArray[2] = const__4;
                Object t2 = null;
                objectArray[3] = t2;
                logger2.debug((String)((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$put_result_handler.invokeStatic(object2);
    }
}


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
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$uuid_prefix
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__2 = 0L;
    public static final Object const__3 = 36L;
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__5 = RT.keyword(null, (String)"event");
    public static final Keyword const__6 = RT.keyword((String)"valcache", (String)"invalid-uuid");
    public static final Keyword const__7 = RT.keyword(null, (String)"s");

    public static Object invokeStatic(Object s) {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke((Object)UUID.fromString((String)((IFn)const__1.getRawRoot()).invoke(s, const__2, const__3)));
        }
        catch (IllegalArgumentException ex) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__5;
                objectArray[1] = const__6;
                objectArray[2] = const__7;
                Object object2 = s;
                s = null;
                objectArray[3] = object2;
                logger2.info((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$uuid_prefix.invokeStatic(object2);
    }
}


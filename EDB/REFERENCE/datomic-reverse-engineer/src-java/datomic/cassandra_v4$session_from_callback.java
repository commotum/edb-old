/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cassandra_v4$session_from_callback
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"session-callback");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"datomic.callback", (String)"create-callback");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object p__10136) {
        Object object;
        Object session_callback;
        Object map__10137;
        Object object2;
        Object object3 = p__10136;
        p__10136 = null;
        Object map__101372 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__101372);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__101372;
            map__101372 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__101372;
            map__101372 = null;
        }
        Object endpoint = map__10137 = object2;
        Object object6 = map__10137;
        map__10137 = null;
        Object object7 = session_callback = RT.get((Object)object6, (Object)const__3);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object temp__5455__auto__10140;
            Logger logger = LoggerFactory.getLogger((String)"datomic.cassandra-v4");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)"Using cassandra-session-callback ", session_callback)));
            }
            Object object8 = temp__5455__auto__10140 = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(session_callback));
            if (object8 != null && object8 != Boolean.FALSE) {
                Object temp__5455__auto__10139;
                Object callback2;
                Object object9 = temp__5455__auto__10140;
                temp__5455__auto__10140 = null;
                Object object10 = callback2 = object9;
                callback2 = null;
                Object object11 = endpoint;
                endpoint = null;
                Object object12 = temp__5455__auto__10139 = ((IFn)object10).invoke(object11);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object session;
                    Object object13 = temp__5455__auto__10139;
                    temp__5455__auto__10139 = null;
                    object = session = object13;
                    session = null;
                } else {
                    Logger logger3 = LoggerFactory.getLogger((String)"datomic.cassandra-v4");
                    if (logger3.isWarnEnabled()) {
                        Logger logger4 = logger3;
                        logger3 = null;
                        Object object14 = session_callback;
                        session_callback = null;
                        logger4.warn((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)"The cassandra-session-callback ", object14, (Object)" nil")));
                    }
                    object = null;
                }
            } else {
                Logger logger5 = LoggerFactory.getLogger((String)"datomic.cassandra-v4");
                if (logger5.isWarnEnabled()) {
                    Logger logger6 = logger5;
                    logger5 = null;
                    Object object15 = session_callback;
                    session_callback = null;
                    logger6.warn((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)"Could not resolve cassandra-session-callback ", object15)));
                }
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
        return cassandra_v4$session_from_callback.invokeStatic(object2);
    }
}


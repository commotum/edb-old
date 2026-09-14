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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class h2$updating_connect
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"user");
    public static final Keyword const__4 = RT.keyword(null, (String)"password");
    public static final Keyword const__5 = RT.keyword(null, (String)"old-user");
    public static final Keyword const__6 = RT.keyword(null, (String)"old-password");
    public static final Var const__7 = RT.var((String)"datomic.h2", (String)"try-connect");
    public static final Keyword const__8 = RT.keyword(null, (String)"data-dir");
    public static final Keyword const__9 = RT.keyword(null, (String)"username");
    public static final Var const__10 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__11 = RT.keyword(null, (String)"event");
    public static final Keyword const__12 = RT.keyword((String)"storage", (String)"update-password");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__14 = RT.var((String)"datomic.sql", (String)"execute-commands");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__17 = RT.var((String)"datomic.h2", (String)"rename-user-cmd");
    public static final Var const__18 = RT.var((String)"datomic.h2", (String)"set-password-cmd");

    public static Object invokeStatic(Object data_dir, Object p__11616) {
        Object object;
        Object object2;
        Object and__5236__auto__11621;
        Object map__11617;
        Object object3;
        Object object4 = p__11616;
        p__11616 = null;
        Object map__116172 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__116172);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__116172;
            map__116172 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__116172;
            map__116172 = null;
        }
        Object args = map__11617 = object3;
        Object user = RT.get((Object)map__11617, (Object)const__3);
        Object password = RT.get((Object)map__11617, (Object)const__4);
        Object old_user = RT.get((Object)map__11617, (Object)const__5);
        Object object7 = map__11617;
        map__11617 = null;
        Object old_password = RT.get((Object)object7, (Object)const__6);
        Object object8 = and__5236__auto__11621 = old_user;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object and__5236__auto__11620;
            Object object9 = and__5236__auto__11620 = user;
            if (object9 != null && object9 != Boolean.FALSE) {
                Object and__5236__auto__11619;
                Object object10 = and__5236__auto__11619 = old_password;
                if (object10 != null && object10 != Boolean.FALSE) {
                    object2 = password;
                    password = null;
                } else {
                    object2 = and__5236__auto__11619;
                    and__5236__auto__11619 = null;
                }
            } else {
                object2 = and__5236__auto__11620;
                and__5236__auto__11620 = null;
            }
        } else {
            object2 = and__5236__auto__11621;
            and__5236__auto__11621 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5457__auto__11622;
            Object[] objectArray = new Object[6];
            objectArray[0] = const__8;
            Object object11 = data_dir;
            data_dir = null;
            objectArray[1] = object11;
            objectArray[2] = const__9;
            Object object12 = old_user;
            old_user = null;
            objectArray[3] = object12;
            objectArray[4] = const__4;
            Object object13 = old_password;
            old_password = null;
            objectArray[5] = object13;
            Object object14 = temp__5457__auto__11622 = ((IFn)const__7.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
            if (object14 != null && object14 != Boolean.FALSE) {
                Object object15 = temp__5457__auto__11622;
                temp__5457__auto__11622 = null;
                Object conn = object15;
                Logger logger = LoggerFactory.getLogger((String)"datomic.h2");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object[] objectArray2 = new Object[4];
                    objectArray2[0] = const__11;
                    objectArray2[1] = const__12;
                    objectArray2[2] = const__3;
                    Object object16 = user;
                    user = null;
                    objectArray2[3] = object16;
                    logger2.info((String)((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray2)));
                }
                Object object17 = ((IFn)const__17.getRawRoot()).invoke(args);
                Object object18 = args;
                args = null;
                ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), conn, ((IFn)const__15.getRawRoot()).invoke(const__16.getRawRoot(), (Object)Tuple.create((Object)object17, (Object)((IFn)const__18.getRawRoot()).invoke(object18))));
                object = conn;
                conn = null;
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return h2$updating_connect.invokeStatic(object3, object4);
    }
}


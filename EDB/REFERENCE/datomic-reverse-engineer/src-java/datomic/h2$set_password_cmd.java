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
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class h2$set_password_cmd
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"user");
    public static final Keyword const__4 = RT.keyword(null, (String)"password");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"format");
    public static final Var const__6 = RT.var((String)"datomic.h2", (String)"uq");

    public static Object invokeStatic(Object p__11612) {
        Object object;
        Object object2;
        Object and__5236__auto__11615;
        Object object3;
        Object object4 = p__11612;
        p__11612 = null;
        Object map__11613 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__11613);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__11613;
            map__11613 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__11613;
            map__11613 = null;
        }
        Object map__116132 = object3;
        Object user = RT.get((Object)map__116132, (Object)const__3);
        Object object7 = map__116132;
        map__116132 = null;
        Object password = RT.get((Object)object7, (Object)const__4);
        Object object8 = and__5236__auto__11615 = user;
        if (object8 != null && object8 != Boolean.FALSE) {
            object2 = password;
        } else {
            object2 = and__5236__auto__11615;
            and__5236__auto__11615 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object9 = user;
            user = null;
            Object object10 = password;
            password = null;
            object = ((IFn)const__5.getRawRoot()).invoke((Object)"alter user %s set password '%s'", ((IFn)const__6.getRawRoot()).invoke(object9), object10);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$set_password_cmd.invokeStatic(object2);
    }
}


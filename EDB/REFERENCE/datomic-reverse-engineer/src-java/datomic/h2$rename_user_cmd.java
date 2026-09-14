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

public final class h2$rename_user_cmd
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"old-user");
    public static final Keyword const__4 = RT.keyword(null, (String)"user");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"format");
    public static final Var const__7 = RT.var((String)"datomic.h2", (String)"uq");

    public static Object invokeStatic(Object p__11607) {
        Object object;
        Object object2;
        Object and__5236__auto__11611;
        Object object3;
        Object and__5236__auto__11610;
        Object object4;
        Object object5 = p__11607;
        p__11607 = null;
        Object map__11608 = object5;
        Object object6 = ((IFn)const__0.getRawRoot()).invoke(map__11608);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = map__11608;
            map__11608 = null;
            object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object7)));
        } else {
            object4 = map__11608;
            map__11608 = null;
        }
        Object map__116082 = object4;
        Object old_user = RT.get((Object)map__116082, (Object)const__3);
        Object object8 = map__116082;
        map__116082 = null;
        Object user = RT.get((Object)object8, (Object)const__4);
        Object object9 = and__5236__auto__11610 = old_user;
        if (object9 != null && object9 != Boolean.FALSE) {
            object3 = user;
        } else {
            object3 = and__5236__auto__11610;
            and__5236__auto__11610 = null;
        }
        Object object10 = and__5236__auto__11611 = object3;
        if (object10 != null && object10 != Boolean.FALSE) {
            object2 = ((IFn)const__5.getRawRoot()).invoke(old_user, user);
        } else {
            object2 = and__5236__auto__11611;
            and__5236__auto__11611 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object11 = old_user;
            old_user = null;
            Object object12 = user;
            user = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)"alter user %s rename to %s", ((IFn)const__7.getRawRoot()).invoke(object11), ((IFn)const__7.getRawRoot()).invoke(object12));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$rename_user_cmd.invokeStatic(object2);
    }
}


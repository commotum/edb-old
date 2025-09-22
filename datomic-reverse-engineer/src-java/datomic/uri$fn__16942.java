/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class uri$fn__16942
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"split");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__2 = Pattern.compile("^datomic:olddev://");
    public static final Object const__3 = Pattern.compile("/");
    public static final Object const__7 = Pattern.compile(":");
    public static final Keyword const__8 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__9 = RT.keyword(null, (String)"olddev");
    public static final Keyword const__10 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__11 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__12 = RT.keyword(null, (String)"params");
    public static final Keyword const__13 = RT.keyword(null, (String)"ip");
    public static final Keyword const__14 = RT.keyword(null, (String)"host");
    public static final Keyword const__15 = RT.keyword(null, (String)"port");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object vec__16943 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, const__2, (Object)""), const__3);
        Object hp = RT.nth((Object)vec__16943, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16943;
        vec__16943 = null;
        Object db_name = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object vec__16946 = ((IFn)const__0.getRawRoot()).invoke(hp, const__7);
        Object host = RT.nth((Object)vec__16946, (int)RT.intCast((long)0L), null);
        Object object3 = vec__16946;
        vec__16946 = null;
        Object port = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__8;
        objectArray[1] = const__9;
        objectArray[2] = const__10;
        Object object4 = hp;
        hp = null;
        objectArray[3] = object4;
        objectArray[4] = const__11;
        Object object5 = db_name;
        db_name = null;
        objectArray[5] = object5;
        objectArray[6] = const__12;
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__13;
        Object[] objectArray3 = new Object[4];
        objectArray3[0] = const__14;
        Object object6 = host;
        host = null;
        objectArray3[1] = object6;
        objectArray3[2] = const__15;
        Object object7 = port;
        port = null;
        objectArray3[3] = Integer.parseInt((String)object7);
        objectArray2[1] = RT.mapUniqueKeys((Object[])objectArray3);
        objectArray[7] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16942.invokeStatic(object2);
    }
}


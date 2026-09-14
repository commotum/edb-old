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

public final class uri$fn__16867
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"split");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__2 = Pattern.compile("^datomic:inf://");
    public static final Object const__3 = Pattern.compile("/");
    public static final Object const__7 = Pattern.compile(":");
    public static final Keyword const__8 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__9 = RT.keyword(null, (String)"inf");
    public static final Keyword const__10 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__11 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__12 = RT.keyword(null, (String)"host");
    public static final Keyword const__13 = RT.keyword(null, (String)"port");

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object and__5236__auto__16875;
        Object object2 = uri2;
        uri2 = null;
        Object vec__16868 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object2, const__2, (Object)""), const__3);
        Object hp = RT.nth((Object)vec__16868, (int)RT.intCast((long)0L), null);
        Object object3 = vec__16868;
        vec__16868 = null;
        Object db_name = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object vec__16871 = ((IFn)const__0.getRawRoot()).invoke(hp, const__7);
        Object host = RT.nth((Object)vec__16871, (int)RT.intCast((long)0L), null);
        Object object4 = vec__16871;
        vec__16871 = null;
        Object port = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        Object[] objectArray = new Object[10];
        objectArray[0] = const__8;
        objectArray[1] = const__9;
        objectArray[2] = const__10;
        Object object5 = hp;
        hp = null;
        objectArray[3] = object5;
        objectArray[4] = const__11;
        Object object6 = db_name;
        db_name = null;
        objectArray[5] = object6;
        objectArray[6] = const__12;
        Object object7 = host;
        host = null;
        objectArray[7] = object7;
        objectArray[8] = const__13;
        Object object8 = and__5236__auto__16875 = port;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = port;
            port = null;
            object = Integer.parseInt((String)object9);
        } else {
            object = and__5236__auto__16875;
            and__5236__auto__16875 = null;
        }
        objectArray[9] = object;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16867.invokeStatic(object2);
    }
}


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

public final class uri$fn__16940
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__1 = Pattern.compile("^datomic:mem://");
    public static final Keyword const__2 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__3 = RT.keyword(null, (String)"mem");
    public static final Keyword const__4 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__5 = RT.keyword(null, (String)"db-name");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object db_name = ((IFn)const__0.getRawRoot()).invoke(object, const__1, (Object)"");
        Object[] objectArray = new Object[6];
        objectArray[0] = const__2;
        objectArray[1] = const__3;
        objectArray[2] = const__4;
        objectArray[3] = "local";
        objectArray[4] = const__5;
        Object object2 = db_name;
        db_name = null;
        objectArray[5] = object2;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16940.invokeStatic(object2);
    }
}


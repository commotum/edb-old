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
import java.net.URI;

public final class uri$fn__16862
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__3 = 1L;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__7 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__8 = RT.keyword(null, (String)"couchbase");
    public static final Keyword const__9 = RT.keyword(null, (String)"host");
    public static final Keyword const__10 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__11 = RT.keyword(null, (String)"db-name");

    public static Object invokeStatic(Object uri2) {
        String q2;
        Object object = uri2;
        uri2 = null;
        URI sub_uri = new URI(new URI((String)object).getSchemeSpecificPart());
        String string = q2 = sub_uri.getQuery();
        q2 = null;
        Object params = ((IFn)const__0.getRawRoot()).invoke((Object)string);
        Object vec__16863 = ((IFn)const__1.getRawRoot()).invoke((Object)((String)((IFn)const__2.getRawRoot()).invoke((Object)sub_uri.getPath(), const__3)).split("/"));
        Object bucket = RT.nth((Object)vec__16863, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16863;
        vec__16863 = null;
        Object dbname = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        URI uRI = sub_uri;
        sub_uri = null;
        String host = uRI.getHost();
        Object[] objectArray = new Object[8];
        objectArray[0] = const__7;
        objectArray[1] = const__8;
        objectArray[2] = const__9;
        String string2 = host;
        host = null;
        objectArray[3] = string2;
        objectArray[4] = const__10;
        Object object3 = bucket;
        bucket = null;
        objectArray[5] = object3;
        objectArray[6] = const__11;
        Object object4 = dbname;
        dbname = null;
        objectArray[7] = object4;
        Object object5 = params;
        params = null;
        return ((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object5);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16862.invokeStatic(object2);
    }
}


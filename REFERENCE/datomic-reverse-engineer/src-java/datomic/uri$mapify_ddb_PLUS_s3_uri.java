/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.net.URI;

public final class uri$mapify_ddb_PLUS_s3_uri
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__3 = 1L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__5 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__6 = RT.keyword(null, (String)"ddb+s3");
    public static final Keyword const__7 = RT.keyword(null, (String)"aws-region");
    public static final Keyword const__8 = RT.keyword(null, (String)"aws-dynamodb-table");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__10 = RT.keyword(null, (String)"db-name");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"last");
    public static final Keyword const__15 = RT.keyword(null, (String)"system");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"second");

    public static Object invokeStatic(Object uri2) {
        IPersistentMap iPersistentMap;
        String q2;
        Object object = uri2;
        uri2 = null;
        URI sub_uri = new URI(new URI((String)object).getSchemeSpecificPart());
        String string = q2 = sub_uri.getQuery();
        q2 = null;
        Object params = ((IFn)const__0.getRawRoot()).invoke((Object)string);
        Object paths = ((IFn)const__1.getRawRoot()).invoke((Object)((String)((IFn)const__2.getRawRoot()).invoke((Object)sub_uri.getPath(), const__3)).split("/"));
        URI uRI = sub_uri;
        sub_uri = null;
        String region = uRI.getHost();
        IFn iFn = (IFn)const__4.getRawRoot();
        Object[] objectArray = new Object[8];
        objectArray[0] = const__5;
        objectArray[1] = const__6;
        objectArray[2] = const__7;
        String string2 = region;
        region = null;
        objectArray[3] = string2;
        objectArray[4] = const__8;
        objectArray[5] = ((IFn)const__9.getRawRoot()).invoke(paths);
        objectArray[6] = const__10;
        objectArray[7] = ((IFn)const__11.getRawRoot()).invoke(paths);
        IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
        if ((long)RT.count((Object)paths) > 2L) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__15;
            Object object2 = paths;
            paths = null;
            objectArray2[1] = ((IFn)const__16.getRawRoot()).invoke(object2);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object object3 = params;
        params = null;
        return iFn.invoke((Object)iPersistentMap2, iPersistentMap, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$mapify_ddb_PLUS_s3_uri.invokeStatic(object2);
    }
}


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

public final class uri$fn__16851
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__3 = 1L;
    public static final Keyword const__4 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__5 = RT.keyword(null, (String)"ddb");
    public static final Keyword const__6 = RT.keyword(null, (String)"region");
    public static final Keyword const__7 = RT.keyword(null, (String)"system-root");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__9 = RT.keyword(null, (String)"db-name");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"second");
    public static final Keyword const__11 = RT.keyword(null, (String)"params");

    public static Object invokeStatic(Object uri2) {
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
        Object[] objectArray = new Object[10];
        objectArray[0] = const__4;
        objectArray[1] = const__5;
        objectArray[2] = const__6;
        String string2 = region;
        region = null;
        objectArray[3] = string2;
        objectArray[4] = const__7;
        objectArray[5] = ((IFn)const__8.getRawRoot()).invoke(paths);
        objectArray[6] = const__9;
        Object object2 = paths;
        paths = null;
        objectArray[7] = ((IFn)const__10.getRawRoot()).invoke(object2);
        objectArray[8] = const__11;
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__5;
        Object object3 = params;
        params = null;
        objectArray2[1] = object3;
        objectArray[9] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16851.invokeStatic(object2);
    }
}


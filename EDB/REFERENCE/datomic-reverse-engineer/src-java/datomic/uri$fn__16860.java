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

public final class uri$fn__16860
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__3 = 1L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__5 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__6 = RT.keyword(null, (String)"s3");
    public static final Keyword const__7 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__8 = RT.keyword(null, (String)"aws-s3-path");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"interpose");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"butlast");
    public static final Keyword const__13 = RT.keyword(null, (String)"db-name");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"last");

    public static Object invokeStatic(Object uri2) {
        String q2;
        Object object = uri2;
        uri2 = null;
        URI sub_uri = new URI(new URI((String)object).getSchemeSpecificPart());
        String string = q2 = sub_uri.getQuery();
        q2 = null;
        Object params = ((IFn)const__0.getRawRoot()).invoke((Object)string);
        Object paths = ((IFn)const__1.getRawRoot()).invoke((Object)((String)((IFn)const__2.getRawRoot()).invoke((Object)sub_uri.getPath(), const__3)).split("/"));
        Object[] objectArray = new Object[8];
        objectArray[0] = const__5;
        objectArray[1] = const__6;
        objectArray[2] = const__7;
        URI uRI = sub_uri;
        sub_uri = null;
        objectArray[3] = uRI.getHost();
        objectArray[4] = const__8;
        objectArray[5] = ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), ((IFn)const__11.getRawRoot()).invoke((Object)"/", ((IFn)const__12.getRawRoot()).invoke(paths)));
        objectArray[6] = const__13;
        Object object2 = paths;
        paths = null;
        objectArray[7] = ((IFn)const__14.getRawRoot()).invoke(object2);
        Object object3 = params;
        params = null;
        return ((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16860.invokeStatic(object2);
    }
}


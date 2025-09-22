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
import java.util.Map;
import java.util.regex.Pattern;

public final class uri$fn__16900
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"fixup-uri-map");
    public static final Var const__3 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__4 = Pattern.compile("^datomic:cass3://");
    public static final Var const__5 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__6 = Pattern.compile("\\?");
    public static final Object const__10 = Pattern.compile("/");
    public static final Object const__12 = Pattern.compile(":");
    public static final Var const__13 = RT.var((String)"datomic.uri", (String)"param-map");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__15 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__16 = RT.keyword(null, (String)"cass3");
    public static final Keyword const__17 = RT.keyword(null, (String)"system-root");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__19 = RT.keyword(null, (String)"host");
    public static final Keyword const__20 = RT.keyword(null, (String)"port");
    public static final Keyword const__21 = RT.keyword(null, (String)"table");
    public static final Keyword const__22 = RT.keyword(null, (String)"db-name");

    public static Object invokeStatic(Object uri2) {
        Object object;
        if (uri2 instanceof Map) {
            Object object2 = uri2;
            uri2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2);
        } else {
            Object object3;
            Object and__5236__auto__16911;
            Object path_query;
            Object object4 = uri2;
            uri2 = null;
            Object object5 = path_query = ((IFn)const__3.getRawRoot()).invoke(object4, const__4, (Object)"");
            path_query = null;
            Object vec__16901 = ((IFn)const__5.getRawRoot()).invoke(object5, const__6);
            Object path2 = RT.nth((Object)vec__16901, (int)RT.intCast((long)0L), null);
            Object object6 = vec__16901;
            vec__16901 = null;
            Object query2 = RT.nth((Object)object6, (int)RT.intCast((long)1L), null);
            Object object7 = path2;
            path2 = null;
            Object vec__16904 = ((IFn)const__5.getRawRoot()).invoke(object7, const__10);
            Object host_port = RT.nth((Object)vec__16904, (int)RT.intCast((long)0L), null);
            Object table = RT.nth((Object)vec__16904, (int)RT.intCast((long)1L), null);
            Object object8 = vec__16904;
            vec__16904 = null;
            Object db_name = RT.nth((Object)object8, (int)RT.intCast((long)2L), null);
            Object vec__16907 = ((IFn)const__5.getRawRoot()).invoke(host_port, const__12);
            Object host = RT.nth((Object)vec__16907, (int)RT.intCast((long)0L), null);
            Object object9 = vec__16907;
            vec__16907 = null;
            Object port = RT.nth((Object)object9, (int)RT.intCast((long)1L), null);
            Object object10 = query2;
            query2 = null;
            Object params = ((IFn)const__13.getRawRoot()).invoke(object10);
            IFn iFn = (IFn)const__14.getRawRoot();
            Object[] objectArray = new Object[12];
            objectArray[0] = const__15;
            objectArray[1] = const__16;
            objectArray[2] = const__17;
            Object object11 = host_port;
            host_port = null;
            objectArray[3] = ((IFn)const__18.getRawRoot()).invoke(object11, (Object)"/", table);
            objectArray[4] = const__19;
            Object object12 = host;
            host = null;
            objectArray[5] = object12;
            objectArray[6] = const__20;
            Object object13 = and__5236__auto__16911 = port;
            if (object13 != null && object13 != Boolean.FALSE) {
                Object object14 = port;
                port = null;
                object3 = Integer.parseInt((String)object14);
            } else {
                object3 = and__5236__auto__16911;
                and__5236__auto__16911 = null;
            }
            objectArray[7] = object3;
            objectArray[8] = const__21;
            Object object15 = table;
            table = null;
            objectArray[9] = object15;
            objectArray[10] = const__22;
            Object object16 = db_name;
            db_name = null;
            objectArray[11] = object16;
            Object object17 = params;
            params = null;
            object = iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object17);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16900.invokeStatic(object2);
    }
}


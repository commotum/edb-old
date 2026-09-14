/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Map;
import java.util.regex.Pattern;

public final class uri$fn__16912
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"fixup-uri-map");
    public static final Var const__3 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__4 = Pattern.compile("datomic:sql://");
    public static final Var const__5 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__6 = Pattern.compile("[?]");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"subs");
    public static final Keyword const__12 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__13 = RT.keyword(null, (String)"sql");
    public static final Keyword const__14 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__15 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__16 = RT.keyword(null, (String)"sql-url");

    public static Object invokeStatic(Object uri2) {
        Object object;
        if (uri2 instanceof Map) {
            Object object2 = uri2;
            uri2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2);
        } else {
            Object object3;
            Object args;
            Object vec__16913;
            Object object4 = uri2;
            uri2 = null;
            Object suffix = ((IFn)const__3.getRawRoot()).invoke(object4, const__4, (Object)"");
            Object object5 = vec__16913 = ((IFn)const__5.getRawRoot()).invoke(suffix, const__6);
            vec__16913 = null;
            Object seq__16914 = ((IFn)const__7.getRawRoot()).invoke(object5);
            Object first__16915 = ((IFn)const__8.getRawRoot()).invoke(seq__16914);
            Object object6 = seq__16914;
            seq__16914 = null;
            Object seq__169142 = ((IFn)const__9.getRawRoot()).invoke(object6);
            Object object7 = first__16915;
            first__16915 = null;
            Object db_name = object7;
            Object object8 = seq__169142;
            seq__169142 = null;
            Object object9 = args = object8;
            args = null;
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10 = suffix;
                Object object11 = suffix;
                suffix = null;
                object3 = ((IFn)const__10.getRawRoot()).invoke(object10, (Object)Numbers.num((long)Numbers.inc((long)((String)object11).indexOf("?"))));
            } else {
                object3 = null;
            }
            Object sql_url2 = object3;
            Object[] objectArray = new Object[8];
            objectArray[0] = const__12;
            objectArray[1] = const__13;
            objectArray[2] = const__14;
            objectArray[3] = sql_url2;
            objectArray[4] = const__15;
            Object object12 = db_name;
            db_name = null;
            objectArray[5] = object12;
            objectArray[6] = const__16;
            Object object13 = sql_url2;
            sql_url2 = null;
            objectArray[7] = object13;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16912.invokeStatic(object2);
    }
}


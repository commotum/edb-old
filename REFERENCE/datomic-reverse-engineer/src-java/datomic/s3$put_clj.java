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
import java.io.ByteArrayInputStream;

public final class s3$put_clj
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__5 = RT.var((String)"datomic.s3", (String)"put-object");
    public static final Keyword const__6 = RT.keyword(null, (String)"contentLength");
    public static final Keyword const__8 = RT.keyword(null, (String)"contentEncoding");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object s32, Object bucket, Object key, Object obj) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, null, (Object)const__3, null));
        try {
            Object s;
            Object object2 = obj;
            obj = null;
            Object object3 = s = ((IFn)const__4.getRawRoot()).invoke(object2);
            s = null;
            byte[] bytes = ((String)object3).getBytes("UTF-8");
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            Object object4 = s32;
            s32 = null;
            Object object5 = bucket;
            bucket = null;
            Object object6 = key;
            key = null;
            ByteArrayInputStream byteArrayInputStream = stream;
            stream = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__6;
            byte[] byArray = bytes;
            bytes = null;
            objectArray[1] = RT.count((Object)byArray);
            objectArray[2] = const__8;
            objectArray[3] = "text/plain; charset=utf-8";
            object = ((IFn)const__5.getRawRoot()).invoke(object4, object5, object6, (Object)byteArrayInputStream, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        finally {
            ((IFn)const__9.getRawRoot()).invoke();
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return s3$put_clj.invokeStatic(object5, object6, object7, object8);
    }
}


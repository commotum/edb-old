/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.HttpMethod
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.HttpMethod;
import datomic.s3.Name;

public final class s3$signed_url
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object method, Object bucket, Object key, Object expiry, Object headers, Object virtual_host_QMARK_) {
        v0 = bucket;
        bucket = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == s3$signed_url.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Name)) {
            v1 = v1;
            s3$signed_url.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = s3$signed_url.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((Name)v1).s3_name();
        }
        bucket_name = v2;
        v3 = key;
        key = null;
        v4 = v3;
        if (Util.classOf((Object)v3) == s3$signed_url.__cached_class__1) ** GOTO lbl19
        if (!(v4 instanceof Name)) {
            v4 = v4;
            s3$signed_url.__cached_class__1 = Util.classOf((Object)v4);
lbl19:
            // 2 sources

            v5 = s3$signed_url.const__1.getRawRoot().invoke(v4);
        } else {
            v5 = ((Name)v4).s3_name();
        }
        key_name = v5;
        v6 = method;
        method = null;
        G__23280 = v6;
        switch (Util.hash((Object)G__23280) >> 17 & 3) {
            case 0: {
                if (G__23280 == s3$signed_url.const__2) {
                    v7 = HttpMethod.GET;
                    break;
                }
                ** GOTO lbl46
            }
            case 1: {
                if (G__23280 == s3$signed_url.const__3) {
                    v7 = HttpMethod.DELETE;
                    break;
                }
                ** GOTO lbl46
            }
            case 2: {
                if (G__23280 == s3$signed_url.const__4) {
                    v7 = HttpMethod.PUT;
                    break;
                }
                ** GOTO lbl46
            }
            case 3: {
                if (G__23280 == s3$signed_url.const__5) {
                    v7 = HttpMethod.HEAD;
                    break;
                }
            }
lbl46:
            // 6 sources

            default: {
                v8 = G__23280;
                G__23280 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)s3$signed_url.const__6.getRawRoot()).invoke((Object)"No matching clause: ", v8));
            }
        }
        method = v7;
        v9 = s3;
        s3 = null;
        v10 = bucket_name;
        bucket_name = null;
        v11 = key_name;
        key_name = null;
        v12 = expiry;
        expiry = null;
        v13 = method;
        method = null;
        return ((IFn)s3$signed_url.const__7.getRawRoot()).invoke(v9, v10, v11, v12, (Object)v13);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object object8 = object;
        object = null;
        Object object9 = object2;
        object2 = null;
        Object object10 = object3;
        object3 = null;
        Object object11 = object4;
        object4 = null;
        Object object12 = object5;
        object5 = null;
        Object object13 = object6;
        object6 = null;
        Object object14 = object7;
        object7 = null;
        return s3$signed_url.invokeStatic(object8, object9, object10, object11, object12, object13, object14);
    }

    public static Object invokeStatic(Object s32, Object method, Object bucket, Object key, Object expiry) {
        Object object = s32;
        s32 = null;
        Object object2 = method;
        method = null;
        Object object3 = bucket;
        bucket = null;
        Object object4 = key;
        key = null;
        Object object5 = expiry;
        expiry = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, object5, (Object)PersistentArrayMap.EMPTY, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return s3$signed_url.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__0 = RT.var((String)"datomic.s3", (String)"signed-url");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__2 = RT.keyword(null, (String)"get");
        const__3 = RT.keyword(null, (String)"delete");
        const__4 = RT.keyword(null, (String)"put");
        const__5 = RT.keyword(null, (String)"head");
        const__6 = RT.var((String)"clojure.core", (String)"str");
        const__7 = RT.var((String)"datomic.s3-api", (String)"generate-presigned-url");
    }
}


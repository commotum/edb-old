/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  com.amazonaws.AmazonServiceException
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import com.amazonaws.AmazonServiceException;

public final class sdkv1$throwable_category
extends AFunction {
    public static final Keyword const__4 = RT.keyword((String)"cognitect.anomalies", (String)"not-found");
    public static final Keyword const__6 = RT.keyword((String)"cognitect.anomalies", (String)"forbidden");
    public static final Keyword const__7 = RT.keyword(null, (String)"default");
    public static final Keyword const__8 = RT.keyword((String)"cognitect.anomalies", (String)"fault");

    public static Object invokeStatic(Object t) {
        Object object;
        if (t instanceof AmazonServiceException) {
            Object object2 = t;
            t = null;
            Object se = object2;
            if (404L == (long)((AmazonServiceException)((Object)se)).getStatusCode()) {
                object = const__4;
            } else {
                Object object3 = se;
                se = null;
                if (403L == (long)((AmazonServiceException)((Object)object3)).getStatusCode()) {
                    object = const__6;
                } else {
                    Keyword keyword = const__7;
                    object = keyword != null && keyword != Boolean.FALSE ? const__8 : null;
                }
            }
        } else {
            Keyword keyword = const__7;
            object = keyword != null && keyword != Boolean.FALSE ? const__8 : null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$throwable_category.invokeStatic(object2);
    }
}


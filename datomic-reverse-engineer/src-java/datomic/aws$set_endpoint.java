/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.amazonaws.AmazonWebServiceClient
 */
package datomic;

import clojure.lang.AFunction;
import com.amazonaws.AmazonWebServiceClient;

public final class aws$set_endpoint
extends AFunction {
    public static Object invokeStatic(Object client2, Object endpoint) {
        Object object = client2;
        client2 = null;
        Object object2 = endpoint;
        endpoint = null;
        ((AmazonWebServiceClient)object).setEndpoint((String)object2);
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws$set_endpoint.invokeStatic(object3, object4);
    }
}


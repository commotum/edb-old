/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
 *  com.amazonaws.services.dynamodbv2.model.DeleteItemRequest
 *  com.amazonaws.services.dynamodbv2.model.DeleteItemResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import datomic.datafy.ObjectToData;

public final class ddb$delete_item
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        DeleteItemResult deleteItemResult = ((AmazonDynamoDBClient)object2).deleteItem((DeleteItemRequest)((IFn)const__1.getRawRoot()).invoke(object3, const__2));
        if (Util.classOf((Object)deleteItemResult) != __cached_class__0) {
            if (deleteItemResult instanceof ObjectToData) {
                object = ((ObjectToData)deleteItemResult).object_to_data();
                return object;
            }
            deleteItemResult = deleteItemResult;
            __cached_class__0 = Util.classOf((Object)deleteItemResult);
        }
        object = const__0.getRawRoot().invoke((Object)deleteItemResult);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$delete_item.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.DeleteItemRequest");
    }
}


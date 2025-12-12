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
 *  com.amazonaws.services.dynamodbv2.model.GetItemRequest
 *  com.amazonaws.services.dynamodbv2.model.GetItemResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import datomic.datafy.ObjectToData;

public final class ddb$get_item_STAR_
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
        GetItemResult getItemResult = ((AmazonDynamoDBClient)object2).getItem((GetItemRequest)((IFn)const__1.getRawRoot()).invoke(object3, const__2));
        if (Util.classOf((Object)getItemResult) != __cached_class__0) {
            if (getItemResult instanceof ObjectToData) {
                object = ((ObjectToData)getItemResult).object_to_data();
                return object;
            }
            getItemResult = getItemResult;
            __cached_class__0 = Util.classOf((Object)getItemResult);
        }
        object = const__0.getRawRoot().invoke((Object)getItemResult);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$get_item_STAR_.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.GetItemRequest");
    }
}


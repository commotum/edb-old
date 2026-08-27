/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.AmazonWebServiceResult
 *  com.amazonaws.ResponseMetadata
 *  com.amazonaws.http.SdkHttpMetadata
 *  com.amazonaws.services.dynamodbv2.model.ConsumedCapacity
 *  com.amazonaws.services.dynamodbv2.model.GetItemResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.dynamodbv2.model.ConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import java.util.Map;

public final class ddb$fn__17546
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"item");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"consumedCapacity");
    public static final Keyword const__6 = RT.keyword(null, (String)"sdkResponseMetadata");
    public static final Keyword const__7 = RT.keyword(null, (String)"sdkHttpMetadata");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        SdkHttpMetadata temp__5457__auto__17555;
        IPersistentVector iPersistentVector2;
        ResponseMetadata temp__5457__auto__17553;
        IPersistentVector iPersistentVector3;
        ConsumedCapacity temp__5457__auto__17551;
        IPersistentVector iPersistentVector4;
        Map temp__5457__auto__17549;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Map map2 = temp__5457__auto__17549 = ((GetItemResult)o).getItem();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__17548;
            Map map3 = temp__5457__auto__17549;
            temp__5457__auto__17549 = null;
            Map map4 = v__17285__auto__17548 = map3;
            v__17285__auto__17548 = null;
            iPersistentVector4 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector4 = null;
        }
        ConsumedCapacity consumedCapacity = temp__5457__auto__17551 = ((GetItemResult)o).getConsumedCapacity();
        if (consumedCapacity != null && consumedCapacity != Boolean.FALSE) {
            ConsumedCapacity v__17285__auto__17550;
            ConsumedCapacity consumedCapacity2 = temp__5457__auto__17551;
            temp__5457__auto__17551 = null;
            ConsumedCapacity consumedCapacity3 = v__17285__auto__17550 = consumedCapacity2;
            v__17285__auto__17550 = null;
            iPersistentVector3 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)consumedCapacity3));
        } else {
            iPersistentVector3 = null;
        }
        ResponseMetadata responseMetadata = temp__5457__auto__17553 = ((AmazonWebServiceResult)o).getSdkResponseMetadata();
        if (responseMetadata != null && responseMetadata != Boolean.FALSE) {
            ResponseMetadata v__17285__auto__17552;
            ResponseMetadata responseMetadata2 = temp__5457__auto__17553;
            temp__5457__auto__17553 = null;
            ResponseMetadata responseMetadata3 = v__17285__auto__17552 = responseMetadata2;
            v__17285__auto__17552 = null;
            iPersistentVector2 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)responseMetadata3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        SdkHttpMetadata sdkHttpMetadata = temp__5457__auto__17555 = ((AmazonWebServiceResult)object2).getSdkHttpMetadata();
        if (sdkHttpMetadata != null && sdkHttpMetadata != Boolean.FALSE) {
            SdkHttpMetadata v__17285__auto__17554;
            SdkHttpMetadata sdkHttpMetadata2 = temp__5457__auto__17555;
            temp__5457__auto__17555 = null;
            SdkHttpMetadata sdkHttpMetadata3 = v__17285__auto__17554 = sdkHttpMetadata2;
            v__17285__auto__17554 = null;
            iPersistentVector = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sdkHttpMetadata3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17546.invokeStatic(object2);
    }
}


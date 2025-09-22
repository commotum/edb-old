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
 *  com.amazonaws.services.dynamodbv2.model.DeleteItemResult
 *  com.amazonaws.services.dynamodbv2.model.ItemCollectionMetrics
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
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.ItemCollectionMetrics;
import java.util.Map;

public final class ddb$fn__17588
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"consumedCapacity");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"itemCollectionMetrics");
    public static final Keyword const__6 = RT.keyword(null, (String)"attributes");
    public static final Keyword const__7 = RT.keyword(null, (String)"sdkResponseMetadata");
    public static final Keyword const__8 = RT.keyword(null, (String)"sdkHttpMetadata");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        SdkHttpMetadata temp__5457__auto__17599;
        IPersistentVector iPersistentVector2;
        ResponseMetadata temp__5457__auto__17597;
        IPersistentVector iPersistentVector3;
        Map temp__5457__auto__17595;
        IPersistentVector iPersistentVector4;
        ItemCollectionMetrics temp__5457__auto__17593;
        IPersistentVector iPersistentVector5;
        ConsumedCapacity temp__5457__auto__17591;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        ConsumedCapacity consumedCapacity = temp__5457__auto__17591 = ((DeleteItemResult)o).getConsumedCapacity();
        if (consumedCapacity != null && consumedCapacity != Boolean.FALSE) {
            ConsumedCapacity v__17285__auto__17590;
            ConsumedCapacity consumedCapacity2 = temp__5457__auto__17591;
            temp__5457__auto__17591 = null;
            ConsumedCapacity consumedCapacity3 = v__17285__auto__17590 = consumedCapacity2;
            v__17285__auto__17590 = null;
            iPersistentVector5 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)consumedCapacity3));
        } else {
            iPersistentVector5 = null;
        }
        ItemCollectionMetrics itemCollectionMetrics = temp__5457__auto__17593 = ((DeleteItemResult)o).getItemCollectionMetrics();
        if (itemCollectionMetrics != null && itemCollectionMetrics != Boolean.FALSE) {
            ItemCollectionMetrics v__17285__auto__17592;
            ItemCollectionMetrics itemCollectionMetrics2 = temp__5457__auto__17593;
            temp__5457__auto__17593 = null;
            ItemCollectionMetrics itemCollectionMetrics3 = v__17285__auto__17592 = itemCollectionMetrics2;
            v__17285__auto__17592 = null;
            iPersistentVector4 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)itemCollectionMetrics3));
        } else {
            iPersistentVector4 = null;
        }
        Map map2 = temp__5457__auto__17595 = ((DeleteItemResult)o).getAttributes();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__17594;
            Map map3 = temp__5457__auto__17595;
            temp__5457__auto__17595 = null;
            Map map4 = v__17285__auto__17594 = map3;
            v__17285__auto__17594 = null;
            iPersistentVector3 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector3 = null;
        }
        ResponseMetadata responseMetadata = temp__5457__auto__17597 = ((AmazonWebServiceResult)o).getSdkResponseMetadata();
        if (responseMetadata != null && responseMetadata != Boolean.FALSE) {
            ResponseMetadata v__17285__auto__17596;
            ResponseMetadata responseMetadata2 = temp__5457__auto__17597;
            temp__5457__auto__17597 = null;
            ResponseMetadata responseMetadata3 = v__17285__auto__17596 = responseMetadata2;
            v__17285__auto__17596 = null;
            iPersistentVector2 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)responseMetadata3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        SdkHttpMetadata sdkHttpMetadata = temp__5457__auto__17599 = ((AmazonWebServiceResult)object2).getSdkHttpMetadata();
        if (sdkHttpMetadata != null && sdkHttpMetadata != Boolean.FALSE) {
            SdkHttpMetadata v__17285__auto__17598;
            SdkHttpMetadata sdkHttpMetadata2 = temp__5457__auto__17599;
            temp__5457__auto__17599 = null;
            SdkHttpMetadata sdkHttpMetadata3 = v__17285__auto__17598 = sdkHttpMetadata2;
            v__17285__auto__17598 = null;
            iPersistentVector = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sdkHttpMetadata3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17588.invokeStatic(object2);
    }
}


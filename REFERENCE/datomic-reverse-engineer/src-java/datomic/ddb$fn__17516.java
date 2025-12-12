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
 *  com.amazonaws.services.dynamodbv2.model.TableDescription
 *  com.amazonaws.services.dynamodbv2.model.UpdateTableResult
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
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;

public final class ddb$fn__17516
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"tableDescription");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"sdkResponseMetadata");
    public static final Keyword const__6 = RT.keyword(null, (String)"sdkHttpMetadata");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        SdkHttpMetadata temp__5457__auto__17523;
        IPersistentVector iPersistentVector2;
        ResponseMetadata temp__5457__auto__17521;
        IPersistentVector iPersistentVector3;
        TableDescription temp__5457__auto__17519;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        TableDescription tableDescription = temp__5457__auto__17519 = ((UpdateTableResult)o).getTableDescription();
        if (tableDescription != null && tableDescription != Boolean.FALSE) {
            TableDescription v__17285__auto__17518;
            TableDescription tableDescription2 = temp__5457__auto__17519;
            temp__5457__auto__17519 = null;
            TableDescription tableDescription3 = v__17285__auto__17518 = tableDescription2;
            v__17285__auto__17518 = null;
            iPersistentVector3 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)tableDescription3));
        } else {
            iPersistentVector3 = null;
        }
        ResponseMetadata responseMetadata = temp__5457__auto__17521 = ((AmazonWebServiceResult)o).getSdkResponseMetadata();
        if (responseMetadata != null && responseMetadata != Boolean.FALSE) {
            ResponseMetadata v__17285__auto__17520;
            ResponseMetadata responseMetadata2 = temp__5457__auto__17521;
            temp__5457__auto__17521 = null;
            ResponseMetadata responseMetadata3 = v__17285__auto__17520 = responseMetadata2;
            v__17285__auto__17520 = null;
            iPersistentVector2 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)responseMetadata3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        SdkHttpMetadata sdkHttpMetadata = temp__5457__auto__17523 = ((AmazonWebServiceResult)object2).getSdkHttpMetadata();
        if (sdkHttpMetadata != null && sdkHttpMetadata != Boolean.FALSE) {
            SdkHttpMetadata v__17285__auto__17522;
            SdkHttpMetadata sdkHttpMetadata2 = temp__5457__auto__17523;
            temp__5457__auto__17523 = null;
            SdkHttpMetadata sdkHttpMetadata3 = v__17285__auto__17522 = sdkHttpMetadata2;
            v__17285__auto__17522 = null;
            iPersistentVector = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sdkHttpMetadata3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17516.invokeStatic(object2);
    }
}


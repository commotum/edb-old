/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.Namespace
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class ddb$loading__6434__auto____17152
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.datafy"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.aws"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"aws"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.edn"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"edn"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ListTablesRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ListTablesResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.CreateTableRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.CreateTableResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.KeySchemaElement"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.KeyType"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ScalarAttributeType"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.PutItemRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.AttributeValue"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.AttributeDefinition"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ReturnValue"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.PutItemResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.GetItemRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.GetItemResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DeleteItemRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DeleteItemResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DeleteTableRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DeleteTableResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.TableDescription"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.UpdateTableRequest"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.UpdateTableResult"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DescribeTableRequest"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.amazonaws.services.dynamodbv2.model.DescribeTableResult"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}


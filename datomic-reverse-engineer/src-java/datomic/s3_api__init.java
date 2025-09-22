/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.MultiFn
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.s3_api$client;
import datomic.s3_api$create_bucket;
import datomic.s3_api$create_bucket_in_region;
import datomic.s3_api$delete_bucket;
import datomic.s3_api$delete_object;
import datomic.s3_api$delete_objects;
import datomic.s3_api$fn__22959;
import datomic.s3_api$fn__22962;
import datomic.s3_api$fn__22970;
import datomic.s3_api$fn__22974;
import datomic.s3_api$fn__22980;
import datomic.s3_api$fn__22990;
import datomic.s3_api$fn__23042;
import datomic.s3_api$fn__23086;
import datomic.s3_api$fn__23096;
import datomic.s3_api$fn__23120;
import datomic.s3_api$fn__23190;
import datomic.s3_api$fn__23196;
import datomic.s3_api$fn__23214;
import datomic.s3_api$fn__23217;
import datomic.s3_api$fn__23220;
import datomic.s3_api$fn__23223;
import datomic.s3_api$fn__23227;
import datomic.s3_api$fn__23231;
import datomic.s3_api$generate_presigned_url;
import datomic.s3_api$get_bucket_policy;
import datomic.s3_api$get_object;
import datomic.s3_api$get_object_metadata;
import datomic.s3_api$list_buckets;
import datomic.s3_api$list_next_batch_of_objects;
import datomic.s3_api$list_objects;
import datomic.s3_api$list_objects_from_request;
import datomic.s3_api$loading__6434__auto____22957;
import datomic.s3_api$put_file;
import datomic.s3_api$put_object;
import datomic.s3_api$put_object_with_canned_acl;
import datomic.s3_api$set_bucket_policy;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class s3_api__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final Object const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Object const__14;
    public static final Object const__15;
    public static final Object const__16;
    public static final Object const__17;
    public static final Object const__18;
    public static final Object const__19;
    public static final Object const__20;
    public static final Object const__21;
    public static final Object const__22;
    public static final Object const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Var const__29;
    public static final Var const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__35;
    public static final AFn const__37;
    public static final AFn const__38;
    public static final AFn const__41;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;
    public static final Var const__53;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__58;
    public static final Var const__59;
    public static final AFn const__61;
    public static final Var const__62;
    public static final AFn const__64;
    public static final Var const__65;
    public static final AFn const__67;
    public static final Var const__68;
    public static final AFn const__70;
    public static final Var const__71;
    public static final AFn const__73;
    public static final Var const__74;
    public static final AFn const__76;
    public static final Var const__77;
    public static final AFn const__79;
    public static final Var const__80;
    public static final AFn const__82;
    public static final Var const__83;
    public static final AFn const__85;
    public static final Var const__86;
    public static final AFn const__88;
    public static final Var const__89;
    public static final AFn const__91;
    public static final Var const__92;
    public static final AFn const__94;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new s3_api$loading__6434__auto____22957()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new s3_api$fn__22959())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new s3_api$client());
        Object object4 = ((IFn)const__10.getRawRoot()).invoke(const__11, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__22962()}));
        Object object5 = ((IFn)const__10.getRawRoot()).invoke(const__14, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__22970()}));
        Object object6 = ((IFn)const__10.getRawRoot()).invoke(const__15, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__22974()}));
        Object object7 = ((IFn)const__10.getRawRoot()).invoke(const__16, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__22980()}));
        Object object8 = ((IFn)const__10.getRawRoot()).invoke(const__17, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__22990()}));
        Object object9 = ((IFn)const__10.getRawRoot()).invoke(const__18, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23042()}));
        Object object10 = ((IFn)const__10.getRawRoot()).invoke(const__19, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23086()}));
        Object object11 = ((IFn)const__10.getRawRoot()).invoke(const__20, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23096()}));
        Object object12 = ((IFn)const__10.getRawRoot()).invoke(const__21, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23120()}));
        Object object13 = ((IFn)const__10.getRawRoot()).invoke(const__22, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23190()}));
        Object object14 = ((IFn)const__10.getRawRoot()).invoke(const__23, const__12.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, new s3_api$fn__23196()}));
        MultiFn multiFn = ((MultiFn)const__24.getRawRoot()).addMethod((Object)const__26, (IFn)new s3_api$fn__23214());
        MultiFn multiFn2 = ((MultiFn)const__24.getRawRoot()).addMethod((Object)const__27, (IFn)new s3_api$fn__23217());
        MultiFn multiFn3 = ((MultiFn)const__24.getRawRoot()).addMethod((Object)const__28, (IFn)new s3_api$fn__23220());
        Object object15 = ((IFn)const__29.getRawRoot()).invoke((Object)const__30, const__31.getRawRoot(), (Object)const__33, const__16);
        MultiFn multiFn4 = ((MultiFn)const__34.getRawRoot()).addMethod((Object)const__35, (IFn)new s3_api$fn__23223());
        Object object16 = ((IFn)const__29.getRawRoot()).invoke((Object)const__30, const__31.getRawRoot(), (Object)const__37, const__23);
        MultiFn multiFn5 = ((MultiFn)const__34.getRawRoot()).addMethod((Object)const__38, (IFn)new s3_api$fn__23227());
        Object object17 = ((IFn)const__29.getRawRoot()).invoke((Object)const__30, const__31.getRawRoot(), (Object)const__41, const__11);
        MultiFn multiFn6 = ((MultiFn)const__34.getRawRoot()).addMethod((Object)const__42, (IFn)new s3_api$fn__23231());
        Var var3 = const__43;
        var3.setMeta((IPersistentMap)const__46);
        Var var4 = var3;
        var3.bindRoot((Object)new s3_api$list_buckets());
        Var var5 = const__47;
        var5.setMeta((IPersistentMap)const__49);
        Var var6 = var5;
        var5.bindRoot((Object)new s3_api$list_objects());
        Var var7 = const__50;
        var7.setMeta((IPersistentMap)const__52);
        Var var8 = var7;
        var7.bindRoot((Object)new s3_api$list_next_batch_of_objects());
        Var var9 = const__53;
        var9.setMeta((IPersistentMap)const__55);
        Var var10 = var9;
        var9.bindRoot((Object)new s3_api$list_objects_from_request());
        Var var11 = const__56;
        var11.setMeta((IPersistentMap)const__58);
        Var var12 = var11;
        var11.bindRoot((Object)new s3_api$create_bucket());
        Var var13 = const__59;
        var13.setMeta((IPersistentMap)const__61);
        Var var14 = var13;
        var13.bindRoot((Object)new s3_api$create_bucket_in_region());
        Var var15 = const__62;
        var15.setMeta((IPersistentMap)const__64);
        Var var16 = var15;
        var15.bindRoot((Object)new s3_api$delete_bucket());
        Var var17 = const__65;
        var17.setMeta((IPersistentMap)const__67);
        Var var18 = var17;
        var17.bindRoot((Object)new s3_api$put_file());
        Var var19 = const__68;
        var19.setMeta((IPersistentMap)const__70);
        Var var20 = var19;
        var19.bindRoot((Object)new s3_api$put_object_with_canned_acl());
        Var var21 = const__71;
        var21.setMeta((IPersistentMap)const__73);
        Var var22 = var21;
        var21.bindRoot((Object)new s3_api$put_object());
        Var var23 = const__74;
        var23.setMeta((IPersistentMap)const__76);
        Var var24 = var23;
        var23.bindRoot((Object)new s3_api$get_object());
        Var var25 = const__77;
        var25.setMeta((IPersistentMap)const__79);
        Var var26 = var25;
        var25.bindRoot((Object)new s3_api$get_object_metadata());
        Var var27 = const__80;
        var27.setMeta((IPersistentMap)const__82);
        Var var28 = var27;
        var27.bindRoot((Object)new s3_api$set_bucket_policy());
        Var var29 = const__83;
        var29.setMeta((IPersistentMap)const__85);
        Var var30 = var29;
        var29.bindRoot((Object)new s3_api$get_bucket_policy());
        Var var31 = const__86;
        var31.setMeta((IPersistentMap)const__88);
        Var var32 = var31;
        var31.bindRoot((Object)new s3_api$delete_object());
        Var var33 = const__89;
        var33.setMeta((IPersistentMap)const__91);
        Var var34 = var33;
        var33.bindRoot((Object)new s3_api$delete_objects());
        Var var35 = const__92;
        var35.setMeta((IPersistentMap)const__94);
        Var var36 = var35;
        var35.bindRoot((Object)new s3_api$generate_presigned_url());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.s3-api");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.s3-api", (String)"client");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create()).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"creds"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"config"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"clojure.core", (String)"extend");
        const__11 = RT.classForName((String)"com.amazonaws.services.s3.model.Bucket");
        const__12 = RT.var((String)"datomic.datafy", (String)"ObjectToData");
        const__13 = RT.keyword(null, (String)"object-to-data");
        const__14 = RT.classForName((String)"com.amazonaws.services.s3.model.BucketPolicy");
        const__15 = RT.classForName((String)"com.amazonaws.services.s3.model.DeleteObjectsResult");
        const__16 = RT.classForName((String)"com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject");
        const__17 = RT.classForName((String)"com.amazonaws.services.s3.model.GeneratePresignedUrlRequest");
        const__18 = RT.classForName((String)"com.amazonaws.services.s3.model.ListObjectsRequest");
        const__19 = RT.classForName((String)"com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError");
        const__20 = RT.classForName((String)"com.amazonaws.services.s3.model.ObjectListing");
        const__21 = RT.classForName((String)"com.amazonaws.services.s3.model.ObjectMetadata");
        const__22 = RT.classForName((String)"com.amazonaws.services.s3.model.Owner");
        const__23 = RT.classForName((String)"com.amazonaws.services.s3.model.S3ObjectSummary");
        const__24 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__26 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.s3.model.ListObjectsRequest"));
        const__27 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.s3.model.ObjectListing"));
        const__28 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.s3.model.ObjectMetadata"));
        const__29 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__30 = RT.var((String)"datomic.datafy", (String)"list-property-types");
        const__31 = RT.var((String)"clojure.core", (String)"assoc");
        const__33 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.model.DeleteObjectsResult"), (Object)RT.keyword(null, (String)"getDeletedObjects"));
        const__34 = RT.var((String)"datomic.datafy", (String)"property-to-object");
        const__35 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.model.DeleteObjectsResult"), (Object)RT.keyword(null, (String)"getDeletedObjects"));
        const__37 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.model.ObjectListing"), (Object)RT.keyword(null, (String)"getObjectSummaries"));
        const__38 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.model.ObjectListing"), (Object)RT.keyword(null, (String)"getObjectSummaries"));
        const__41 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), (Object)RT.keyword(null, (String)"listBuckets"));
        const__42 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), (Object)RT.keyword(null, (String)"listBuckets"));
        const__43 = RT.var((String)"datomic.s3-api", (String)"list-buckets");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")}))))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.s3-api", (String)"list-objects");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.s3-api", (String)"list-next-batch-of-objects");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__53 = RT.var((String)"datomic.s3-api", (String)"list-objects-from-request");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.s3-api", (String)"create-bucket");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__59 = RT.var((String)"datomic.s3-api", (String)"create-bucket-in-region");
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2")))), RT.keyword(null, (String)"column"), 1});
        const__62 = RT.var((String)"datomic.s3-api", (String)"delete-bucket");
        const__64 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__65 = RT.var((String)"datomic.s3-api", (String)"put-file");
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2"), (Object)Symbol.intern(null, (String)"x3")))), RT.keyword(null, (String)"column"), 1});
        const__68 = RT.var((String)"datomic.s3-api", (String)"put-object-with-canned-acl");
        const__70 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__71 = RT.var((String)"datomic.s3-api", (String)"put-object");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2"), (Object)Symbol.intern(null, (String)"x3"), (Object)Symbol.intern(null, (String)"x4")))), RT.keyword(null, (String)"column"), 1});
        const__74 = RT.var((String)"datomic.s3-api", (String)"get-object");
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2")))), RT.keyword(null, (String)"column"), 1});
        const__77 = RT.var((String)"datomic.s3-api", (String)"get-object-metadata");
        const__79 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2")))), RT.keyword(null, (String)"column"), 1});
        const__80 = RT.var((String)"datomic.s3-api", (String)"set-bucket-policy");
        const__82 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2")))), RT.keyword(null, (String)"column"), 1});
        const__83 = RT.var((String)"datomic.s3-api", (String)"get-bucket-policy");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__86 = RT.var((String)"datomic.s3-api", (String)"delete-object");
        const__88 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2")))), RT.keyword(null, (String)"column"), 1});
        const__89 = RT.var((String)"datomic.s3-api", (String)"delete-objects");
        const__91 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__92 = RT.var((String)"datomic.s3-api", (String)"generate-presigned-url");
        const__94 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.s3.AmazonS3Client"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.s3.AmazonS3Client")})), (Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2"), (Object)Symbol.intern(null, (String)"x3"), (Object)Symbol.intern(null, (String)"x4")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        s3_api__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.s3_api__init").getClassLoader());
        try {
            s3_api__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
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
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.uri$create_h2;
import datomic.uri$db_uri;
import datomic.uri$fixup_uri_map;
import datomic.uri$fn__16822;
import datomic.uri$fn__16835;
import datomic.uri$fn__16851;
import datomic.uri$fn__16853;
import datomic.uri$fn__16856;
import datomic.uri$fn__16860;
import datomic.uri$fn__16862;
import datomic.uri$fn__16867;
import datomic.uri$fn__16876;
import datomic.uri$fn__16888;
import datomic.uri$fn__16900;
import datomic.uri$fn__16912;
import datomic.uri$fn__16927;
import datomic.uri$fn__16929;
import datomic.uri$fn__16931;
import datomic.uri$fn__16940;
import datomic.uri$fn__16942;
import datomic.uri$fn__16950;
import datomic.uri$fn__16957;
import datomic.uri$fn__16971;
import datomic.uri$fn__16975;
import datomic.uri$fn__16984;
import datomic.uri$fn__16993;
import datomic.uri$fn__17002;
import datomic.uri$fn__17011;
import datomic.uri$fn__17016;
import datomic.uri$fn__17020;
import datomic.uri$fn__17023;
import datomic.uri$fn__17027;
import datomic.uri$fn__17031;
import datomic.uri$fn__17044;
import datomic.uri$fn__17046;
import datomic.uri$fn__17048;
import datomic.uri$fn__17052;
import datomic.uri$loading__6434__auto____16820;
import datomic.uri$loggable_cluster_conf;
import datomic.uri$map__GT_query_string;
import datomic.uri$mapify_ddb_PLUS_s3_uri;
import datomic.uri$param_map;
import datomic.uri$parse;
import datomic.uri$parse_db;
import datomic.uri$parse_h2;
import datomic.uri$parse_query_string;
import datomic.uri$query_args;
import datomic.uri$query_key;
import datomic.uri$read_port;
import datomic.uri$remove_query_string;
import datomic.uri$storage_protocol;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class uri__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final Keyword const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final AFn const__27;
    public static final Keyword const__28;
    public static final Keyword const__29;
    public static final Keyword const__30;
    public static final Keyword const__31;
    public static final Keyword const__32;
    public static final Keyword const__33;
    public static final Keyword const__34;
    public static final Keyword const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Keyword const__39;
    public static final Keyword const__40;
    public static final Keyword const__41;
    public static final Keyword const__42;
    public static final Keyword const__43;
    public static final Keyword const__44;
    public static final Var const__45;
    public static final AFn const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__62;
    public static final Var const__63;
    public static final Var const__64;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__69;
    public static final Var const__70;
    public static final AFn const__72;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new uri$loading__6434__auto____16820()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new uri$fn__16822())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new uri$parse_query_string());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new uri$read_port());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new uri$storage_protocol());
        Object object3 = ((IFn)new uri$fn__16835()).invoke();
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new uri$fixup_uri_map());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new uri$param_map());
        MultiFn multiFn = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__22, (IFn)new uri$fn__16851());
        MultiFn multiFn2 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__23, (IFn)new uri$fn__16853());
        Var var11 = const__24;
        var11.setMeta((IPersistentMap)const__27);
        Var var12 = var11;
        var11.bindRoot((Object)new uri$mapify_ddb_PLUS_s3_uri());
        MultiFn multiFn3 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__28, (IFn)new uri$fn__16856());
        MultiFn multiFn4 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__29, (IFn)new uri$fn__16860());
        MultiFn multiFn5 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__30, (IFn)new uri$fn__16862());
        MultiFn multiFn6 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__31, (IFn)new uri$fn__16867());
        MultiFn multiFn7 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__32, (IFn)new uri$fn__16876());
        MultiFn multiFn8 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__33, (IFn)new uri$fn__16888());
        MultiFn multiFn9 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__34, (IFn)new uri$fn__16900());
        MultiFn multiFn10 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__35, (IFn)new uri$fn__16912());
        Var var13 = const__36;
        var13.setMeta((IPersistentMap)const__38);
        Var var14 = var13;
        var13.bindRoot((Object)new uri$parse_h2());
        MultiFn multiFn11 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__39, (IFn)new uri$fn__16927());
        MultiFn multiFn12 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__40, (IFn)new uri$fn__16929());
        MultiFn multiFn13 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__41, (IFn)new uri$fn__16931());
        MultiFn multiFn14 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__42, (IFn)new uri$fn__16940());
        MultiFn multiFn15 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__43, (IFn)new uri$fn__16942());
        MultiFn multiFn16 = ((MultiFn)const__21.getRawRoot()).addMethod((Object)const__44, (IFn)new uri$fn__16950());
        Var var15 = const__45;
        var15.setMeta((IPersistentMap)const__47);
        Var var16 = var15;
        var15.bindRoot((Object)new uri$parse());
        Var var17 = const__48;
        var17.setMeta((IPersistentMap)const__50);
        Var var18 = var17;
        var17.bindRoot((Object)new uri$parse_db());
        Var var19 = const__51;
        var19.setMeta((IPersistentMap)const__53);
        Var var20 = var19;
        var19.bindRoot((Object)new uri$remove_query_string());
        Var var21 = const__54;
        var21.setMeta((IPersistentMap)const__56);
        Var var22 = var21;
        var21.bindRoot((Object)new uri$loggable_cluster_conf());
        Object object4 = ((IFn)new uri$fn__16957()).invoke();
        Var var23 = const__57;
        var23.setMeta((IPersistentMap)const__59);
        Var var24 = var23;
        var23.bindRoot((Object)new uri$query_key());
        Var var25 = const__60;
        var25.setMeta((IPersistentMap)const__62);
        Var var26 = var25;
        var25.bindRoot((Object)new uri$query_args());
        MultiFn multiFn17 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__22, (IFn)new uri$fn__16971());
        MultiFn multiFn18 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__28, (IFn)new uri$fn__16975());
        MultiFn multiFn19 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__32, (IFn)new uri$fn__16984());
        MultiFn multiFn20 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__33, (IFn)new uri$fn__16993());
        MultiFn multiFn21 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__34, (IFn)new uri$fn__17002());
        MultiFn multiFn22 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__23, (IFn)new uri$fn__17011());
        MultiFn multiFn23 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__29, (IFn)new uri$fn__17016());
        MultiFn multiFn24 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__30, (IFn)new uri$fn__17020());
        MultiFn multiFn25 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__43, (IFn)new uri$fn__17023());
        MultiFn multiFn26 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__31, (IFn)new uri$fn__17027());
        MultiFn multiFn27 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__35, (IFn)new uri$fn__17031());
        Var var27 = const__64;
        var27.setMeta((IPersistentMap)const__66);
        Var var28 = var27;
        var27.bindRoot((Object)new uri$map__GT_query_string());
        Var var29 = const__67;
        var29.setMeta((IPersistentMap)const__69);
        Var var30 = var29;
        var29.bindRoot((Object)new uri$create_h2());
        MultiFn multiFn28 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__40, (IFn)new uri$fn__17044());
        MultiFn multiFn29 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__39, (IFn)new uri$fn__17046());
        MultiFn multiFn30 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__41, (IFn)new uri$fn__17048());
        MultiFn multiFn31 = ((MultiFn)const__63.getRawRoot()).addMethod((Object)const__42, (IFn)new uri$fn__17052());
        Var var31 = const__70;
        var31.setMeta((IPersistentMap)const__72);
        Var var32 = var31;
        var31.bindRoot((Object)new uri$db_uri());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.uri");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.uri", (String)"parse-query-string");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.uri", (String)"read-port");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"portstr")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.uri", (String)"storage-protocol");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.uri", (String)"fixup-uri-map");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.uri", (String)"param-map");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"query")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.uri", (String)"parse*");
        const__22 = RT.keyword(null, (String)"ddb");
        const__23 = RT.keyword(null, (String)"ddb-local");
        const__24 = RT.var((String)"datomic.uri", (String)"mapify-ddb+s3-uri");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.keyword(null, (String)"ddb+s3");
        const__29 = RT.keyword(null, (String)"s3");
        const__30 = RT.keyword(null, (String)"couchbase");
        const__31 = RT.keyword(null, (String)"inf");
        const__32 = RT.keyword(null, (String)"cass");
        const__33 = RT.keyword(null, (String)"cass2");
        const__34 = RT.keyword(null, (String)"cass3");
        const__35 = RT.keyword(null, (String)"sql");
        const__36 = RT.var((String)"datomic.uri", (String)"parse-h2");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.keyword(null, (String)"limited-edition");
        const__40 = RT.keyword(null, (String)"dev");
        const__41 = RT.keyword(null, (String)"mdev");
        const__42 = RT.keyword(null, (String)"mem");
        const__43 = RT.keyword(null, (String)"olddev");
        const__44 = RT.keyword(null, (String)"default");
        const__45 = RT.var((String)"datomic.uri", (String)"parse");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.uri", (String)"parse-db");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.uri", (String)"remove-query-string");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.uri", (String)"loggable-cluster-conf");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.uri", (String)"query-key");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.uri", (String)"query-args");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.uri", (String)"create");
        const__64 = RT.var((String)"datomic.uri", (String)"map->query-string");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.uri", (String)"create-h2");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__70 = RT.var((String)"datomic.uri", (String)"db-uri");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri"), (Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        uri__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.uri__init").getClassLoader());
        try {
            uri__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


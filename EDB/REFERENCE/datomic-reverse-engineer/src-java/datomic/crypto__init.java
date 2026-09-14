/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
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
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.crypto$append_hmac;
import datomic.crypto$calc_hmac;
import datomic.crypto$cipher;
import datomic.crypto$combine_fragments;
import datomic.crypto$crypt_bbuf;
import datomic.crypto$decrypt;
import datomic.crypto$decrypt_pk;
import datomic.crypto$deser_key;
import datomic.crypto$encrypt;
import datomic.crypto$encrypt_pk;
import datomic.crypto$fn__23340;
import datomic.crypto$fn__23364;
import datomic.crypto$genkey;
import datomic.crypto$keystore;
import datomic.crypto$load_private_key;
import datomic.crypto$load_public_key;
import datomic.crypto$loading__6434__auto____23338;
import datomic.crypto$mac;
import datomic.crypto$random_bytes;
import datomic.crypto$random_string;
import datomic.crypto$read_n;
import datomic.crypto$ser_key;
import datomic.crypto$sign;
import datomic.crypto$spec__GT_public_key;
import datomic.crypto$split_array;
import datomic.crypto$split_key;
import datomic.crypto$validate_hmac;
import datomic.crypto$verify;
import datomic.crypto$xor_arrays;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class crypto__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__41;
    public static final Var const__42;
    public static final AFn const__44;
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
    public static final AFn const__64;
    public static final Var const__65;
    public static final Var const__66;
    public static final AFn const__68;
    public static final Var const__69;
    public static final AFn const__71;
    public static final Var const__72;
    public static final AFn const__74;
    public static final Var const__75;
    public static final AFn const__77;
    public static final Var const__78;
    public static final AFn const__80;
    public static final Var const__81;
    public static final AFn const__83;
    public static final Var const__84;
    public static final AFn const__86;
    public static final Var const__87;
    public static final AFn const__89;
    public static final Var const__90;
    public static final AFn const__92;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new crypto$loading__6434__auto____23338()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new crypto$fn__23340())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new crypto$random_string());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot((Object)new crypto$cipher());
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new crypto$genkey());
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__20);
        Var var8 = var7;
        var7.bindRoot((Object)new crypto$mac());
        Var var9 = const__21;
        var9.setMeta((IPersistentMap)const__23);
        Var var10 = var9;
        var9.bindRoot((Object)new crypto$encrypt_pk());
        Var var11 = const__24;
        var11.setMeta((IPersistentMap)const__26);
        Var var12 = var11;
        var11.bindRoot((Object)new crypto$spec__GT_public_key());
        Var var13 = const__27;
        var13.setMeta((IPersistentMap)const__29);
        Var var14 = var13;
        var13.bindRoot((Object)new crypto$decrypt_pk());
        Var var15 = const__30;
        var15.setMeta((IPersistentMap)const__32);
        Var var16 = var15;
        var15.bindRoot((Object)new crypto$encrypt());
        Var var17 = const__33;
        var17.setMeta((IPersistentMap)const__35);
        Var var18 = var17;
        var17.bindRoot((Object)new crypto$decrypt());
        Var var19 = const__36;
        var19.setMeta((IPersistentMap)const__38);
        Var var20 = var19;
        var19.bindRoot((Object)new SecureRandom());
        Var var21 = const__39;
        var21.setMeta((IPersistentMap)const__41);
        Var var22 = var21;
        var21.bindRoot((Object)new crypto$random_bytes());
        Var var23 = const__42;
        var23.setMeta((IPersistentMap)const__44);
        Var var24 = var23;
        var23.bindRoot((Object)new crypto$read_n());
        Var var25 = const__45;
        var25.setMeta((IPersistentMap)const__47);
        Var var26 = var25;
        var25.bindRoot((Object)new crypto$xor_arrays());
        Var var27 = const__48;
        var27.setMeta((IPersistentMap)const__50);
        Var var28 = var27;
        var27.bindRoot((Object)new crypto$split_array());
        Var var29 = const__51;
        var29.setMeta((IPersistentMap)const__53);
        Var var30 = var29;
        var29.bindRoot((Object)new crypto$split_key());
        Var var31 = const__54;
        var31.setMeta((IPersistentMap)const__56);
        Var var32 = var31;
        var31.bindRoot((Object)new crypto$ser_key());
        Var var33 = const__57;
        var33.setMeta((IPersistentMap)const__59);
        Var var34 = var33;
        var33.bindRoot((Object)new crypto$deser_key());
        Var var35 = const__60;
        var35.setMeta((IPersistentMap)const__62);
        Var var36 = var35;
        var35.bindRoot((Object)new crypto$combine_fragments());
        Var var37 = const__63;
        var37.setMeta((IPersistentMap)const__64);
        Var var38 = var37;
        var37.bindRoot(((IFn)const__65.getRawRoot()).invoke((Object)new crypto$fn__23364()));
        Var var39 = const__66;
        var39.setMeta((IPersistentMap)const__68);
        Var var40 = var39;
        var39.bindRoot((Object)new crypto$calc_hmac());
        Var var41 = const__69;
        var41.setMeta((IPersistentMap)const__71);
        Var var42 = var41;
        var41.bindRoot((Object)new crypto$append_hmac());
        Var var43 = const__72;
        var43.setMeta((IPersistentMap)const__74);
        Var var44 = var43;
        var43.bindRoot((Object)new crypto$validate_hmac());
        Var var45 = const__75;
        var45.setMeta((IPersistentMap)const__77);
        Var var46 = var45;
        var45.bindRoot((Object)new crypto$crypt_bbuf());
        Var var47 = const__78;
        var47.setMeta((IPersistentMap)const__80);
        Var var48 = var47;
        var47.bindRoot((Object)new crypto$sign());
        Var var49 = const__81;
        var49.setMeta((IPersistentMap)const__83);
        Var var50 = var49;
        var49.bindRoot((Object)new crypto$verify());
        Var var51 = const__84;
        var51.setMeta((IPersistentMap)const__86);
        Var var52 = var51;
        var51.bindRoot((Object)new crypto$keystore());
        Var var53 = const__87;
        var53.setMeta((IPersistentMap)const__89);
        Var var54 = var53;
        var53.bindRoot((Object)new crypto$load_private_key());
        Var var55 = const__90;
        var55.setMeta((IPersistentMap)const__92);
        Var var56 = var55;
        var55.bindRoot((Object)new crypto$load_public_key());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.crypto");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.crypto", (String)"random-string");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"entropy"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.crypto", (String)"cipher");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"javax.crypto.Cipher"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"name")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.crypto", (String)"genkey");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cipher-name")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.crypto", (String)"mac");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"plaintext")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"secret")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.crypto", (String)"encrypt-pk");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"plaintext"), (Object)((IObj)Symbol.intern(null, (String)"key")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PrivateKey")}))))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.crypto", (String)"spec->public-key");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"algo"), (Object)Symbol.intern(null, (String)"b64encoded")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.crypto", (String)"decrypt-pk");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ciphertext"), (Object)((IObj)Symbol.intern(null, (String)"key")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PublicKey")}))))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.crypto", (String)"encrypt");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"plaintext"), (Object)Symbol.intern(null, (String)"cipher-name"), (Object)Symbol.intern(null, (String)"rawkey")))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.crypto", (String)"decrypt");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ciphertext"), (Object)Symbol.intern(null, (String)"cipher-name"), (Object)Symbol.intern(null, (String)"rawkey")))), RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.crypto", (String)"random");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.security.SecureRandom"), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.crypto", (String)"random-bytes");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.crypto", (String)"read-n");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")})), (Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.crypto", (String)"xor-arrays");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"b1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")})), (Object)((IObj)Symbol.intern(null, (String)"b2")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")}))))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.crypto", (String)"split-array");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"b1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")}))))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.crypto", (String)"split-key");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"k")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Key")}))))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.crypto", (String)"ser-key");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"k")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Key")}))))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.crypto", (String)"deser-key");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"alg"))})))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.crypto", (String)"combine-fragments");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f1"), (Object)Symbol.intern(null, (String)"f2")))), RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.crypto", (String)"hmac-length");
        const__64 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__65 = RT.var((String)"clojure.core", (String)"memoize");
        const__66 = RT.var((String)"datomic.crypto", (String)"calc-hmac");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bbuf")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")})), (Object)Symbol.intern(null, (String)"alg"), (Object)Symbol.intern(null, (String)"k"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")})))), RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.crypto", (String)"append-hmac");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bbuf")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")})), (Object)Symbol.intern(null, (String)"alg"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__72 = RT.var((String)"datomic.crypto", (String)"validate-hmac");
        const__74 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bbuf")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")})), (Object)Symbol.intern(null, (String)"alg"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__75 = RT.var((String)"datomic.crypto", (String)"crypt-bbuf");
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"input"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"input")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")})), (Object)((IObj)Symbol.intern(null, (String)"k")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SecretKey")})), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"extra")))), RT.keyword(null, (String)"column"), 1});
        const__78 = RT.var((String)"datomic.crypto", (String)"sign");
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"algo"), (Object)Symbol.intern(null, (String)"priv"), (Object)Symbol.intern(null, (String)"arr")))), RT.keyword(null, (String)"column"), 1});
        const__81 = RT.var((String)"datomic.crypto", (String)"verify");
        const__83 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"algo"), (Object)((IObj)Symbol.intern(null, (String)"pub")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PublicKey")})), (Object)Symbol.intern(null, (String)"arr"), (Object)Symbol.intern(null, (String)"sig")))), RT.keyword(null, (String)"column"), 1});
        const__84 = RT.var((String)"datomic.crypto", (String)"keystore");
        const__86 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"password")), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"password"), (Object)Symbol.intern(null, (String)"type")))), RT.keyword(null, (String)"column"), 1});
        const__87 = RT.var((String)"datomic.crypto", (String)"load-private-key");
        const__89 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"ks")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"KeyStore")})), (Object)Symbol.intern(null, (String)"alias"), (Object)Symbol.intern(null, (String)"password")))), RT.keyword(null, (String)"column"), 1});
        const__90 = RT.var((String)"datomic.crypto", (String)"load-public-key");
        const__92 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"ks")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"KeyStore")})), (Object)Symbol.intern(null, (String)"alias")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        crypto__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.crypto__init").getClassLoader());
        try {
            crypto__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


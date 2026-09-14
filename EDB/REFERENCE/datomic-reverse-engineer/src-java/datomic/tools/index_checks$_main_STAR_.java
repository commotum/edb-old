/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index_checks$_main_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"db-resources");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"db");
    public static final Keyword const__6 = RT.keyword(null, (String)"log");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__8 = RT.var((String)"datomic.tools", (String)"pretty-datom");
    public static final Var const__9 = RT.var((String)"datomic.tools.index-checks", (String)"progress-dot-fn");
    public static final Object const__10 = 1000L;
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__12 = 0L;
    public static final Var const__13 = RT.var((String)"datomic.tools", (String)"nohistory-checker");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__15 = RT.var((String)"datomic.tools.index-checks", (String)"boot-tail-collision?");
    public static final Var const__16 = RT.var((String)"datomic.tools.index-checks", (String)"card-one-collisions");
    public static final Keyword const__17 = RT.keyword(null, (String)"eavt");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"prn");
    public static final Keyword const__24 = RT.keyword(null, (String)"type");
    public static final Keyword const__25 = RT.keyword(null, (String)"card-1-collision");
    public static final Keyword const__26 = RT.keyword(null, (String)"d1");
    public static final Keyword const__27 = RT.keyword(null, (String)"d2");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__36 = RT.var((String)"datomic.tools.index-checks", (String)"unique-collisions");
    public static final Keyword const__37 = RT.keyword(null, (String)"unique-collision");
    public static final Var const__38 = RT.var((String)"datomic.tools.index-checks", (String)"log-only");
    public static final Var const__39 = RT.var((String)"datomic.api", (String)"history");
    public static final Keyword const__40 = RT.keyword(null, (String)"log-only");
    public static final Keyword const__41 = RT.keyword(null, (String)"d");
    public static final Keyword const__42 = RT.keyword(null, (String)"summary");
    public static final Keyword const__43 = RT.keyword(null, (String)"problems");
    public static final Var const__44 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object uri2) {
        Object d2;
        Object d1;
        Object d22;
        Object d12;
        Object object;
        Object object2 = uri2;
        uri2 = null;
        Object cr = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object map__21925 = ((IFn)const__1.getRawRoot()).invoke(cr);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(map__21925);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__21925;
            map__21925 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object4)));
        } else {
            object = map__21925;
            map__21925 = null;
        }
        Object map__219252 = object;
        Object db2 = RT.get((Object)map__219252, (Object)const__5);
        Object object5 = map__219252;
        map__219252 = null;
        RT.get((Object)object5, (Object)const__6);
        Object p = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), db2);
        Object progress = ((IFn)const__9.getRawRoot()).invoke(const__10);
        Object probs = ((IFn)const__11.getRawRoot()).invoke(const__12);
        Object nohist_QMARK_ = ((IFn)const__13.getRawRoot()).invoke(db2);
        Object seq_21926 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), ((IFn)const__16.getRawRoot()).invoke(db2, (Object)const__17, progress)));
        Object chunk_21927 = null;
        long count_21928 = 0L;
        long i_21929 = 0L;
        while (true) {
            Object temp__5457__auto__21952;
            if (i_21929 < count_21928) {
                Object vec__21930 = ((Indexed)chunk_21927).nth(RT.intCast((long)i_21929));
                d12 = RT.nth((Object)vec__21930, (int)RT.intCast((long)0L), null);
                Object object6 = vec__21930;
                vec__21930 = null;
                d22 = RT.nth((Object)object6, (int)RT.intCast((long)1L), null);
                ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
                Object[] objectArray = new Object[6];
                objectArray[0] = const__24;
                objectArray[1] = const__25;
                objectArray[2] = const__26;
                Object object7 = d12;
                d12 = null;
                objectArray[3] = ((IFn)p).invoke(object7);
                objectArray[4] = const__27;
                Object object8 = d22;
                d22 = null;
                objectArray[5] = ((IFn)p).invoke(object8);
                ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object9 = seq_21926;
                seq_21926 = null;
                Object object10 = chunk_21927;
                chunk_21927 = null;
                ++i_21929;
                chunk_21927 = object10;
                seq_21926 = object9;
                continue;
            }
            Object object11 = seq_21926;
            seq_21926 = null;
            Object object12 = temp__5457__auto__21952 = ((IFn)const__3.getRawRoot()).invoke(object11);
            if (object12 == null || object12 == Boolean.FALSE) break;
            Object object13 = temp__5457__auto__21952;
            temp__5457__auto__21952 = null;
            Object seq_219262 = object13;
            Object object14 = ((IFn)const__29.getRawRoot()).invoke(seq_219262);
            if (object14 != null && object14 != Boolean.FALSE) {
                Object c__5719__auto__21951 = ((IFn)const__30.getRawRoot()).invoke(seq_219262);
                Object object15 = seq_219262;
                seq_219262 = null;
                Object object16 = c__5719__auto__21951;
                Object object17 = c__5719__auto__21951;
                c__5719__auto__21951 = null;
                i_21929 = RT.intCast((long)0L);
                count_21928 = RT.intCast((int)RT.count((Object)object17));
                chunk_21927 = object16;
                seq_21926 = ((IFn)const__31.getRawRoot()).invoke(object15);
                continue;
            }
            Object vec__21933 = ((IFn)const__34.getRawRoot()).invoke(seq_219262);
            d1 = RT.nth((Object)vec__21933, (int)RT.intCast((long)0L), null);
            Object object18 = vec__21933;
            vec__21933 = null;
            d2 = RT.nth((Object)object18, (int)RT.intCast((long)1L), null);
            ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
            Object[] objectArray = new Object[6];
            objectArray[0] = const__24;
            objectArray[1] = const__25;
            objectArray[2] = const__26;
            Object object19 = d1;
            d1 = null;
            objectArray[3] = ((IFn)p).invoke(object19);
            objectArray[4] = const__27;
            Object object20 = d2;
            d2 = null;
            objectArray[5] = ((IFn)p).invoke(object20);
            ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
            Object object21 = seq_219262;
            seq_219262 = null;
            i_21929 = 0L;
            count_21928 = 0L;
            chunk_21927 = null;
            seq_21926 = ((IFn)const__35.getRawRoot()).invoke(object21);
        }
        Object seq_21936 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__36.getRawRoot()).invoke(db2, progress));
        Object chunk_21937 = null;
        long count_21938 = 0L;
        long i_21939 = 0L;
        while (true) {
            Object temp__5457__auto__21954;
            if (i_21939 < count_21938) {
                Object vec__21940 = ((Indexed)chunk_21937).nth(RT.intCast((long)i_21939));
                d12 = RT.nth((Object)vec__21940, (int)RT.intCast((long)0L), null);
                Object object22 = vec__21940;
                vec__21940 = null;
                d22 = RT.nth((Object)object22, (int)RT.intCast((long)1L), null);
                ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
                Object[] objectArray = new Object[6];
                objectArray[0] = const__24;
                objectArray[1] = const__37;
                objectArray[2] = const__26;
                Object object23 = d12;
                d12 = null;
                objectArray[3] = ((IFn)p).invoke(object23);
                objectArray[4] = const__27;
                Object object24 = d22;
                d22 = null;
                objectArray[5] = ((IFn)p).invoke(object24);
                ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object25 = seq_21936;
                seq_21936 = null;
                Object object26 = chunk_21937;
                chunk_21937 = null;
                ++i_21939;
                chunk_21937 = object26;
                seq_21936 = object25;
                continue;
            }
            Object object27 = seq_21936;
            seq_21936 = null;
            Object object28 = temp__5457__auto__21954 = ((IFn)const__3.getRawRoot()).invoke(object27);
            if (object28 == null || object28 == Boolean.FALSE) break;
            Object object29 = temp__5457__auto__21954;
            temp__5457__auto__21954 = null;
            Object seq_219362 = object29;
            Object object30 = ((IFn)const__29.getRawRoot()).invoke(seq_219362);
            if (object30 != null && object30 != Boolean.FALSE) {
                Object c__5719__auto__21953 = ((IFn)const__30.getRawRoot()).invoke(seq_219362);
                Object object31 = seq_219362;
                seq_219362 = null;
                Object object32 = c__5719__auto__21953;
                Object object33 = c__5719__auto__21953;
                c__5719__auto__21953 = null;
                i_21939 = RT.intCast((long)0L);
                count_21938 = RT.intCast((int)RT.count((Object)object33));
                chunk_21937 = object32;
                seq_21936 = ((IFn)const__31.getRawRoot()).invoke(object31);
                continue;
            }
            Object vec__21943 = ((IFn)const__34.getRawRoot()).invoke(seq_219362);
            d1 = RT.nth((Object)vec__21943, (int)RT.intCast((long)0L), null);
            Object object34 = vec__21943;
            vec__21943 = null;
            d2 = RT.nth((Object)object34, (int)RT.intCast((long)1L), null);
            ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
            Object[] objectArray = new Object[6];
            objectArray[0] = const__24;
            objectArray[1] = const__37;
            objectArray[2] = const__26;
            Object object35 = d1;
            d1 = null;
            objectArray[3] = ((IFn)p).invoke(object35);
            objectArray[4] = const__27;
            Object object36 = d2;
            d2 = null;
            objectArray[5] = ((IFn)p).invoke(object36);
            ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
            Object object37 = seq_219362;
            seq_219362 = null;
            i_21939 = 0L;
            count_21938 = 0L;
            chunk_21937 = null;
            seq_21936 = ((IFn)const__35.getRawRoot()).invoke(object37);
        }
        Object object38 = nohist_QMARK_;
        nohist_QMARK_ = null;
        Object object39 = cr;
        cr = null;
        Object object40 = db2;
        db2 = null;
        Object object41 = progress;
        progress = null;
        Object seq_21946 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object38, ((IFn)const__38.getRawRoot()).invoke(object39, ((IFn)const__39.getRawRoot()).invoke(object40), object41)));
        Object chunk_21947 = null;
        long count_21948 = 0L;
        long i_21949 = 0L;
        while (true) {
            Object temp__5457__auto__21956;
            if (i_21949 < count_21948) {
                Object d = ((Indexed)chunk_21947).nth(RT.intCast((long)i_21949));
                ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
                Object[] objectArray = new Object[4];
                objectArray[0] = const__24;
                objectArray[1] = const__40;
                objectArray[2] = const__41;
                Object object42 = d;
                d = null;
                objectArray[3] = ((IFn)p).invoke(object42);
                ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object43 = seq_21946;
                seq_21946 = null;
                Object object44 = chunk_21947;
                chunk_21947 = null;
                ++i_21949;
                chunk_21947 = object44;
                seq_21946 = object43;
                continue;
            }
            Object object45 = seq_21946;
            seq_21946 = null;
            Object object46 = temp__5457__auto__21956 = ((IFn)const__3.getRawRoot()).invoke(object45);
            if (object46 == null || object46 == Boolean.FALSE) break;
            Object object47 = temp__5457__auto__21956;
            temp__5457__auto__21956 = null;
            Object seq_219462 = object47;
            Object object48 = ((IFn)const__29.getRawRoot()).invoke(seq_219462);
            if (object48 != null && object48 != Boolean.FALSE) {
                Object c__5719__auto__21955 = ((IFn)const__30.getRawRoot()).invoke(seq_219462);
                Object object49 = seq_219462;
                seq_219462 = null;
                Object object50 = c__5719__auto__21955;
                Object object51 = c__5719__auto__21955;
                c__5719__auto__21955 = null;
                i_21949 = RT.intCast((long)0L);
                count_21948 = RT.intCast((int)RT.count((Object)object51));
                chunk_21947 = object50;
                seq_21946 = ((IFn)const__31.getRawRoot()).invoke(object49);
                continue;
            }
            Object d = ((IFn)const__34.getRawRoot()).invoke(seq_219462);
            ((IFn)const__21.getRawRoot()).invoke(probs, const__22.getRawRoot());
            Object[] objectArray = new Object[4];
            objectArray[0] = const__24;
            objectArray[1] = const__40;
            objectArray[2] = const__41;
            Object object52 = d;
            d = null;
            objectArray[3] = ((IFn)p).invoke(object52);
            ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
            Object object53 = seq_219462;
            seq_219462 = null;
            i_21949 = 0L;
            count_21948 = 0L;
            chunk_21947 = null;
            seq_21946 = ((IFn)const__35.getRawRoot()).invoke(object53);
        }
        Object[] objectArray = new Object[4];
        objectArray[0] = const__24;
        objectArray[1] = const__42;
        objectArray[2] = const__43;
        Object object54 = probs;
        probs = null;
        objectArray[3] = ((IFn)const__44.getRawRoot()).invoke(object54);
        return ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index_checks$_main_STAR_.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$fn__13205
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"create-simple-alter");
    public static final Keyword const__1 = RT.keyword(null, (String)"unique");
    public static final Keyword const__2 = RT.keyword(null, (String)"isComponent");
    public static final Keyword const__3 = RT.keyword(null, (String)"noHistory");
    public static final AFn const__7 = (AFn)Tuple.create((Object)42L, (Object)37L, (Object)38L);
    public static final AFn const__9 = (AFn)Tuple.create((Object)42L, (Object)RT.keyword(null, (String)"disabled"), (Object)38L);
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"add-unique");
    public static final AFn const__11 = (AFn)Tuple.create((Object)42L, (Object)37L, (Object)RT.keyword(null, (String)"disabled"));
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"drop-unique");
    public static final AFn const__14 = (AFn)Tuple.create((Object)43L, (Object)RT.keyword(null, (String)"disabled"), (Object)Boolean.TRUE);
    public static final AFn const__16 = (AFn)Tuple.create((Object)44L, (Object)Boolean.TRUE, (Object)RT.keyword(null, (String)"disabled"));
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"drop-avet");
    public static final AFn const__19 = (AFn)Tuple.create((Object)45L, (Object)Boolean.TRUE, (Object)RT.keyword(null, (String)"disabled"));
    public static final AFn const__20 = (AFn)Tuple.create((Object)42L, (Object)38L, (Object)RT.keyword(null, (String)"disabled"));
    public static final AFn const__24 = (AFn)Tuple.create((Object)41L, (Object)35L, (Object)36L);
    public static final Var const__25 = RT.var((String)"datomic.db", (String)"card-one->card-many");
    public static final AFn const__26 = (AFn)Tuple.create((Object)44L, (Object)RT.keyword(null, (String)"disabled"), (Object)Boolean.TRUE);
    public static final Var const__27 = RT.var((String)"datomic.db", (String)"add-avet");
    public static final AFn const__28 = (AFn)Tuple.create((Object)45L, (Object)RT.keyword(null, (String)"disabled"), (Object)Boolean.TRUE);
    public static final AFn const__29 = (AFn)Tuple.create((Object)43L, (Object)Boolean.TRUE, (Object)RT.keyword(null, (String)"disabled"));
    public static final AFn const__30 = (AFn)Tuple.create((Object)42L, (Object)RT.keyword(null, (String)"disabled"), (Object)37L);
    public static final AFn const__31 = (AFn)Tuple.create((Object)41L, (Object)36L, (Object)35L);
    public static final Var const__32 = RT.var((String)"datomic.db", (String)"card-many->card-one");
    public static final AFn const__33 = (AFn)Tuple.create((Object)42L, (Object)38L, (Object)37L);

    public static Object invokeStatic() {
        Object simple_unique = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object simple_is_component = ((IFn)const__0.getRawRoot()).invoke((Object)const__2);
        Object simple_no_history = ((IFn)const__0.getRawRoot()).invoke((Object)const__3);
        Object[] objectArray = new Object[28];
        objectArray[0] = const__7;
        objectArray[1] = simple_unique;
        objectArray[2] = const__9;
        objectArray[3] = const__10.getRawRoot();
        objectArray[4] = const__11;
        objectArray[5] = const__12.getRawRoot();
        objectArray[6] = const__14;
        objectArray[7] = simple_is_component;
        objectArray[8] = const__16;
        objectArray[9] = const__17.getRawRoot();
        objectArray[10] = const__19;
        objectArray[11] = simple_no_history;
        objectArray[12] = const__20;
        objectArray[13] = const__12.getRawRoot();
        objectArray[14] = const__24;
        objectArray[15] = const__25.getRawRoot();
        objectArray[16] = const__26;
        objectArray[17] = const__27.getRawRoot();
        objectArray[18] = const__28;
        Object object = simple_no_history;
        simple_no_history = null;
        objectArray[19] = object;
        objectArray[20] = const__29;
        Object object2 = simple_is_component;
        simple_is_component = null;
        objectArray[21] = object2;
        objectArray[22] = const__30;
        objectArray[23] = const__10.getRawRoot();
        objectArray[24] = const__31;
        objectArray[25] = const__32.getRawRoot();
        objectArray[26] = const__33;
        Object object3 = simple_unique;
        simple_unique = null;
        objectArray[27] = object3;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke() {
        return db$fn__13205.invokeStatic();
    }
}


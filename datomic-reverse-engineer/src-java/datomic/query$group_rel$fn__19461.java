/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import datomic.query$group_rel$reify__19452;
import java.util.ArrayList;

public final class query$group_rel$fn__19461
extends AFunction {
    long r;
    Object srel;
    Object cmp;
    Object row;
    public static final Keyword const__3 = RT.keyword(null, (String)"else");

    public query$group_rel$fn__19461(long l, Object object, Object object2, Object object3) {
        this.r = l;
        this.srel = object;
        this.cmp = object2;
        this.row = object3;
    }

    public Object invoke() {
        Number number;
        block2: {
            long nr = Numbers.inc((long)this.r);
            while (true) {
                if (nr == (long)((ArrayList)this.srel).size()) {
                    number = Numbers.num((long)nr);
                    break block2;
                }
                if ((long)((query$group_rel$reify__19452)this.cmp).compare(this.row, ((ArrayList)this.srel).get(RT.intCast((long)nr))) != 0L) break;
                nr = Numbers.inc((long)nr);
            }
            Keyword keyword = const__3;
            number = keyword != null && keyword != Boolean.FALSE ? Numbers.num((long)nr) : null;
        }
        return number;
    }
}


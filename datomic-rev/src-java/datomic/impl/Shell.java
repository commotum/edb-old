/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.impl;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

public class Shell {
    private static final Var REQUIRE = RT.var((String)"clojure.core", (String)"require");

    public static void help() {
        try {
            int n;
            InputStream stm = Shell.class.getResourceAsStream("/shell-help.txt");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(stm));
            StringWriter writer2 = new StringWriter();
            char[] buffer = new char[1024];
            while ((n = reader2.read(buffer)) != -1) {
                writer2.write(buffer, 0, n);
            }
            System.out.println(writer2);
        }
        catch (IOException ioe) {
            System.out.println("Failed to load help information.");
        }
    }

    public static Reader loadFile(String filename) throws FileNotFoundException {
        return new BufferedReader(new FileReader(filename));
    }

    static {
        REQUIRE.invoke((Object)Symbol.intern((String)"clojure.pprint"));
    }
}


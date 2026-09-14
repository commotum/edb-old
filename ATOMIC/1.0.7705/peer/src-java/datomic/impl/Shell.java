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

/**
 * Support routines for the interactive Datomic shell. The class loads shell
 * help text, opens script files, and ensures Clojure pretty-printing is
 * available to the shell runtime.
 */
public class Shell {
    private static final Var REQUIRE = RT.var((String)"clojure.core", (String)"require");

    public static void help() {
        try {
            InputStream stm = Shell.class.getResourceAsStream("/shell-help.txt");
            Reader reader2 = new BufferedReader(new InputStreamReader(stm));
            StringWriter writer2 = new StringWriter();
            char[] buffer = new char[1024];
            int n;
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

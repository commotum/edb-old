(do
  (clojure.core/in-ns 'datomic.cli)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.cli)
    {:doc
     "Command line parsing.\n\nFunctions in this namespace deal with the following data structures:\n\nCommand line args\nA sequence of strings, e.g. from *command-line-args*\n\n(Processed) args\nargs converted into map of keys to typed values\n\nSpecs             \nA set of descriptions of arguments. Specs are maps that contain\nsome or all of the following keys:\n\n* short-name   a single chararcter name\n* long-name    a human-friendly Clojure name\n* coerce       a function to coerce a string to desired type\n* required     true if the arg must be present\n* default      a default value\n* doc          a docstring for the argument\n\nPositions\nA collection of keywords specifying the names to bind to positional\narguments. The names must correspond to the :long-name field in the\nspec map.\n\nA program that had a required, positional argument dir and an optional,\nnamed argument port might start like this:\n\n(def args (cli/parse-or-exit!\n           *file*\n           *command-line-args*\n           #{{:long-name :dir :required true}\n             {:long-name :port :short-name :p :coerce #(Integer. %)}}\n           [:dir]))\n"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.set :as 'set]
        ['clojure.java.io :as 'io]
        ['clojure.java.shell :as 'sh]
        ['clojure.string :as 'str]
        ['datomic.require :as 'req])))
  (when-not (.equals 'datomic.cli 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.cli))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.set :as 'set]
          ['clojure.java.io :as 'io]
          ['clojure.java.shell :as 'sh]
          ['clojure.string :as 'str]
          ['datomic.require :as 'req]))))
  (def unique-index
   (fn unique_index
     ([xrel k v]
       (reduce
         (fn fn__15373
           ([m x]
             (let [temp__5802__auto__ (get x k)]
               (if temp__5802__auto__
                 (let [xk temp__5802__auto__ temp__5802__auto__ (if v (get x v) x)]
                   (if temp__5802__auto__ (let [xv temp__5802__auto__] (assoc m xk xv)) m))
                 m))))
         {}
         xrel))
     ([xrel k] (unique-index xrel k nil))))
  (reset-meta!
    #'unique-index
    (assoc
      {:private true, :arglists (clojure.core/list ['xrel 'k] ['xrel 'k 'v]), :column (int 1)}
      :name
      'unique-index
      :ns
      *ns*))
  (defn print-help
    ([cmd spec positions]
      (binding [*out* *err*]
        (let [pset (into #{} positions)
              positional? (fn positional_QMARK_
                            ([p1__15378#] (contains? pset (:long-name p1__15378#))))
              named (remove positional? spec)
              positional (filter positional? spec)]
          (apply println "Usage:" (str cmd (when (seq named) " {options}")) (map name positions))
          (when (seq named)
            (println "\nOptions: ")
            (loop [seq_15381 (seq named) chunk_15382 nil count_15383 0 i_15384 0]
              (if (< i_15384 count_15383)
                (let [opt (.nth ^clojure.lang.Indexed chunk_15382 (int i_15384))]
                  (println
                    (str
                      (format
                        "%-20s: "
                        (str
                          (let [temp__5804__auto__ (:short-name opt)]
                            (when temp__5804__auto__
                              (let [sn temp__5804__auto__] (str "-" (name sn) ",\n "))))
                          "--"
                          (name (:long-name opt))))
                      (:doc opt)
                      (let [temp__5804__auto__ (:default opt)]
                        (when temp__5804__auto__
                          (let [df temp__5804__auto__] (str " (default " df ")"))))))
                  (recur seq_15381 chunk_15382 count_15383 (inc i_15384)))
                (let [temp__5804__auto__ (seq seq_15381)]
                  (when temp__5804__auto__
                    (let [seq_15381 temp__5804__auto__]
                      (if (chunked-seq? seq_15381)
                        (let [c__6065__auto__ (chunk-first seq_15381)]
                          (recur
                            (chunk-rest seq_15381)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [opt (first seq_15381)]
                          (println
                            (str
                              (format
                                "%-20s: "
                                (str
                                  (let [temp__5804__auto__ (:short-name opt)]
                                    (when temp__5804__auto__
                                      (let [sn temp__5804__auto__] (str "-" (name sn) ",\n "))))
                                  "--"
                                  (name (:long-name opt))))
                              (:doc opt)
                              (let [temp__5804__auto__ (:default opt)]
                                (when temp__5804__auto__
                                  (let [df temp__5804__auto__] (str " (default " df ")"))))))
                          (recur (next seq_15381) nil 0 0)))))))))
          (when (seq positional)
            (println "\nPositional arguments:")
            (let [idx (unique-index positional :long-name)]
              (loop [seq_15385 (seq positions) chunk_15386 nil count_15387 0 i_15388 0]
                (if (< i_15388 count_15387)
                  (let [pos (.nth ^clojure.lang.Indexed chunk_15386 (int i_15388))]
                    (let [temp__5804__auto__ (get idx pos)]
                      (when temp__5804__auto__
                        (let [arg temp__5804__auto__]
                          (println
                            (str
                              (format "%-20s: " (name (:long-name arg)))
                              (:doc arg)
                              (let [temp__5804__auto__ (:default arg)]
                                (when temp__5804__auto__
                                  (let [df temp__5804__auto__] (str " (default " df ")")))))))))
                    (recur seq_15385 chunk_15386 count_15387 (inc i_15388)))
                  (let [temp__5804__auto__ (seq seq_15385)]
                    (when temp__5804__auto__
                      (let [seq_15385 temp__5804__auto__]
                        (if (chunked-seq? seq_15385)
                          (let [c__6065__auto__ (chunk-first seq_15385)]
                            (recur
                              (chunk-rest seq_15385)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [pos (first seq_15385)]
                            (let [temp__5804__auto__ (get idx pos)]
                              (when temp__5804__auto__
                                (let [arg temp__5804__auto__]
                                  (println
                                    (str
                                      (format "%-20s: " (name (:long-name arg)))
                                      (:doc arg)
                                      (let [temp__5804__auto__ (:default arg)]
                                        (when temp__5804__auto__
                                          (let [df temp__5804__auto__]
                                            (str " (default " df ")")))))))))
                            (recur (next seq_15385) nil 0 0)))))))))
            nil)))))
  (reset-meta!
    #'print-help
    (assoc
      {:arglists (clojure.core/list ['cmd 'spec 'positions]), :column (int 1)}
      :name
      'print-help
      :ns
      *ns*))
  (defn cli->map
    ([strings positions vararg]
      (let [G__15405 strings
            vec__15406 G__15405
            seq__15407 (seq vec__15406)
            first__15408 (first seq__15407)
            seq__15407 (next seq__15407)
            s first__15408
            more seq__15407
            positions positions
            m {}]
        (loop [G__15405 G__15405 positions positions m m]
          (let [vec__15409 G__15405
                seq__15410 (seq vec__15409)
                first__15411 (first seq__15410)
                seq__15410 (next seq__15410)
                s first__15411
                more seq__15410
                positions positions
                m m]
            (if s
              (let [temp__5802__auto__ (re-matches #"-+(.*)" s)]
                (if temp__5802__auto__
                  (let [vec__15412 temp__5802__auto__
                        _ (nth vec__15412 (int 0) nil)
                        k (nth vec__15412 (int 1) nil)]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (let [vec__15415 more
                            seq__15416 (seq vec__15415)
                            first__15417 (first seq__15416)
                            seq__15416 (next seq__15416)
                            v first__15417
                            more seq__15416]
                        (recur more positions (assoc m (keyword k) v)))))
                  (let [vec__15418 positions
                        seq__15419 (seq vec__15418)
                        first__15420 (first seq__15419)
                        seq__15419 (next seq__15419)
                        k first__15420
                        pmore seq__15419]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (recur more pmore (assoc m (keyword k) s))))))
              m))))))
  (reset-meta!
    #'cli->map
    (assoc
      {:arglists (clojure.core/list ['strings 'positions 'vararg]), :column (int 1)}
      :name
      'cli->map
      :ns
      *ns*))
  (defn expand-short-names
    ([m spec] (let [idx (unique-index spec :short-name :long-name)] (set/rename-keys m idx))))
  (reset-meta!
    #'expand-short-names
    (assoc
      {:private true, :arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'expand-short-names
      :ns
      *ns*))
  (defn coerce-vals
    ([m spec]
      (let [idx (unique-index spec :long-name :coerce)]
        (reduce
          (fn fn__15425
            ([m p__15424]
              (let [vec__15426 p__15424
                    k (nth vec__15426 (int 0) nil)
                    v (nth vec__15426 (int 1) nil)]
                (assoc m k ((get idx k identity) v)))))
          {}
          m))))
  (reset-meta!
    #'coerce-vals
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'coerce-vals
      :ns
      *ns*))
  (defn apply-defaults
    ([m spec] (let [defaults (unique-index spec :long-name :default)] (merge defaults m))))
  (reset-meta!
    #'apply-defaults
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'apply-defaults
      :ns
      *ns*))
  (defn missing-values
    ([m spec]
      (let [required (into #{} (keys (unique-index spec :long-name :required)))]
        (set/difference required (set (keys m))))))
  (reset-meta!
    #'missing-values
    (assoc
      {:arglists (clojure.core/list ['m 'spec]), :column (int 1)}
      :name
      'missing-values
      :ns
      *ns*))
  (def parse-or-exit!
   (fn parse_or_exit_BANG_
     ([cmd args spec positions vararg]
       (if (some #{"--help"} args)
         (do (print-help cmd spec positions) (java.lang.System/exit (int -1)) nil)
         (let [m (apply-defaults
                   (coerce-vals (expand-short-names (cli->map args positions vararg) spec) spec)
                   spec)
               missing (missing-values m spec)]
           (if (seq missing)
             (do
               (print-help cmd spec positions)
               (println "\n**** missing required arguments" missing "****")
               (java.lang.System/exit (int -1))
               nil)
             m))))
     ([cmd arg spec positions] (parse-or-exit! cmd arg spec positions nil))))
  (reset-meta!
    #'parse-or-exit!
    (assoc
      {:arglists
       (clojure.core/list ['cmd 'arg 'spec 'positions] ['cmd 'args 'spec 'positions 'vararg]),
       :column (int 1)}
      :name
      'parse-or-exit!
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cli" "failed") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cli" "failed") (atom false))
  (defn fail ([msg] (reset! failed true) msg))
  (reset-meta!
    #'fail
    (assoc {:arglists (clojure.core/list ['msg]), :column (int 1)} :name 'fail :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.cli" "exit-after-command") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.cli" "exit-after-command") (atom true))
  (defn shell
    ([& args]
      (let [map__15435 (apply sh/sh args)
            map__15435 (if (seq? map__15435)
                         (if (next map__15435)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15435))
                           (if (seq map__15435) (first map__15435) {}))
                         map__15435)
            m map__15435
            exit (get map__15435 :exit)
            err (get map__15435 :err)]
        (when-not (or (= 0 exit) (seq err))
          (throw (ex-info "Shell command failed" {:args args, :result m})))
        (:out m))))
  (reset-meta!
    #'shell
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name 'shell :ns *ns*))
  (defn development-version
    ([& _]
      (let [map__15438 (read-string (slurp "pom.clj"))
            map__15438 (if (seq? map__15438)
                         (if (next map__15438)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__15438))
                           (if (seq map__15438) (first map__15438) {}))
                         map__15438)
            version_prefix (get map__15438 :version-prefix)
            revision (str/trimr (shell "build/revision"))]
        (str version_prefix "." revision))))
  (reset-meta!
    #'development-version
    (assoc
      {:arglists (clojure.core/list ['& '_]), :column (int 1)}
      :name
      'development-version
      :ns
      *ns*)))
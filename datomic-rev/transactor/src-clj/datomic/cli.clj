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
  (defn unique-index
    ([xrel k v]
      (reduce
        (fn fn__20377
          ([m x]
            (let [temp__5802__auto__ (get x k)]
              (if temp__5802__auto__
                (let [xk temp__5802__auto__ temp__5802__auto__ (if v (get x v) x)]
                  (if temp__5802__auto__ (let [xv temp__5802__auto__] (assoc m xk xv)) m))
                m))))
        {}
        xrel))
    ([xrel k] (unique-index xrel k nil)))
  (reset-meta!
    #'unique-index
    (assoc
      {:private true, :arglists (clojure.core/list ['xrel 'k] ['xrel 'k 'v]), :column 1}
      :name
      'unique-index
      :ns
      *ns*))
  (defn print-help
    ([cmd spec positions]
      (binding [*out* *err*]
        (let [pset (into #{} positions)
              positional? (fn positional_QMARK_
                            ([p1__20382#] (contains? pset (:long-name p1__20382#))))
              named (remove positional? spec)
              positional (filter positional? spec)]
          (apply println "Usage:" (str cmd (when (seq named) " {options}")) (map name positions))
          (when (seq named)
            (println "\nOptions: ")
            (loop [seq_20385 (seq named) chunk_20386 nil count_20387 0 i_20388 0]
              (if (< i_20388 count_20387)
                (let [opt (.nth ^clojure.lang.Indexed chunk_20386 (int i_20388))]
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
                  (recur seq_20385 chunk_20386 count_20387 (inc i_20388)))
                (let [temp__5804__auto__ (seq seq_20385)]
                  (when temp__5804__auto__
                    (let [seq_20385 temp__5804__auto__]
                      (if (chunked-seq? seq_20385)
                        (let [c__6065__auto__ (chunk-first seq_20385)]
                          (recur
                            (chunk-rest seq_20385)
                            c__6065__auto__
                            (int (count c__6065__auto__))
                            (int 0)))
                        (let [opt (first seq_20385)]
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
                          (recur (next seq_20385) nil 0 0)))))))))
          (when (seq positional)
            (println "\nPositional arguments:")
            (let [idx (unique-index positional :long-name)]
              (loop [seq_20389 (seq positions) chunk_20390 nil count_20391 0 i_20392 0]
                (if (< i_20392 count_20391)
                  (let [pos (.nth ^clojure.lang.Indexed chunk_20390 (int i_20392))]
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
                    (recur seq_20389 chunk_20390 count_20391 (inc i_20392)))
                  (let [temp__5804__auto__ (seq seq_20389)]
                    (when temp__5804__auto__
                      (let [seq_20389 temp__5804__auto__]
                        (if (chunked-seq? seq_20389)
                          (let [c__6065__auto__ (chunk-first seq_20389)]
                            (recur
                              (chunk-rest seq_20389)
                              c__6065__auto__
                              (int (count c__6065__auto__))
                              (int 0)))
                          (let [pos (first seq_20389)]
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
                            (recur (next seq_20389) nil 0 0)))))))))
            nil)))))
  (defn cli->map
    ([strings positions vararg]
      (let [G__20409 strings
            vec__20410 G__20409
            seq__20411 (seq vec__20410)
            first__20412 (first seq__20411)
            seq__20411 (next seq__20411)
            s first__20412
            more seq__20411
            positions positions
            m {}]
        (loop [G__20409 G__20409 positions positions m m]
          (let [vec__20413 G__20409
                seq__20414 (seq vec__20413)
                first__20415 (first seq__20414)
                seq__20414 (next seq__20414)
                s first__20415
                more seq__20414
                positions positions
                m m]
            (if s
              (let [temp__5802__auto__ (re-matches #"-+(.*)" s)]
                (if temp__5802__auto__
                  (let [vec__20416 temp__5802__auto__
                        _ (nth vec__20416 (int 0) nil)
                        k (nth vec__20416 (int 1) nil)]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (let [vec__20419 more
                            seq__20420 (seq vec__20419)
                            first__20421 (first seq__20420)
                            seq__20420 (next seq__20420)
                            v first__20421
                            more seq__20420]
                        (recur more positions (assoc m (keyword k) v)))))
                  (let [vec__20422 positions
                        seq__20423 (seq vec__20422)
                        first__20424 (first seq__20423)
                        seq__20423 (next seq__20423)
                        k first__20424
                        pmore seq__20423]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (recur more pmore (assoc m (keyword k) s))))))
              m))))))
  (defn expand-short-names
    ([m spec] (let [idx (unique-index spec :short-name :long-name)] (set/rename-keys m idx))))
  (reset-meta!
    #'expand-short-names
    (assoc
      {:private true, :arglists (clojure.core/list ['m 'spec]), :column 1}
      :name
      'expand-short-names
      :ns
      *ns*))
  (defn coerce-vals
    ([m spec]
      (let [idx (unique-index spec :long-name :coerce)]
        (reduce
          (fn fn__20429
            ([m p__20428]
              (let [vec__20430 p__20428
                    k (nth vec__20430 (int 0) nil)
                    v (nth vec__20430 (int 1) nil)]
                (assoc m k ((get idx k identity) v)))))
          {}
          m))))
  (defn apply-defaults
    ([m spec] (let [defaults (unique-index spec :long-name :default)] (merge defaults m))))
  (defn missing-values
    ([m spec]
      (let [required (into #{} (keys (unique-index spec :long-name :required)))]
        (set/difference required (set (keys m))))))
  (defn parse-or-exit!
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
    ([cmd arg spec positions] (parse-or-exit! cmd arg spec positions nil)))
  (def failed (atom false))
  (defn fail ([msg] (reset! failed true) msg))
  (def exit-after-command (atom true))
  (defn shell
    ([& args]
      (let [map__20439 (apply sh/sh args)
            map__20439 (if (seq? map__20439)
                         (if (next map__20439)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20439))
                           (if (seq map__20439) (first map__20439) {}))
                         map__20439)
            m map__20439
            exit (get map__20439 :exit)
            err (get map__20439 :err)]
        (when-not (or (= 0 exit) (seq err))
          (throw (ex-info "Shell command failed" {:args args, :result m})))
        (:out m))))
  (defn development-version
    ([& _]
      (let [map__20442 (read-string (slurp "pom.clj"))
            map__20442 (if (seq? map__20442)
                         (if (next map__20442)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__20442))
                           (if (seq map__20442) (first map__20442) {}))
                         map__20442)
            version_prefix (get map__20442 :version-prefix)
            revision (str/trimr (shell "build/revision"))]
        (str version_prefix "." revision)))))
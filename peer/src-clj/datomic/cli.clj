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
        (fn fn__20666
          ([m x]
            (let [temp__5455__auto__ (get x k)]
              (if temp__5455__auto__
                (let [xk temp__5455__auto__ temp__5455__auto__ (if v (get x v) x)]
                  (if temp__5455__auto__ (let [xv temp__5455__auto__] (assoc m xk xv)) m))
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
                            ([p1__20671#] (contains? pset (:long-name p1__20671#))))
              named (remove positional? spec)
              positional (filter positional? spec)]
          (apply println "Usage:" (str cmd (when (seq named) " {options}")) (map name positions))
          (when (seq named)
            (println "\nOptions: ")
            (loop [seq_20674 (seq named) chunk_20675 nil count_20676 0 i_20677 0]
              (if (< i_20677 count_20676)
                (let [opt (.nth ^clojure.lang.Indexed chunk_20675 (int i_20677))]
                  (println
                    (str
                      (format
                        "%-20s: "
                        (str
                          (let [temp__5457__auto__ (:short-name opt)]
                            (when temp__5457__auto__
                              (let [sn temp__5457__auto__] (str "-" (name sn) ",\n "))))
                          "--"
                          (name (:long-name opt))))
                      (:doc opt)
                      (let [temp__5457__auto__ (:default opt)]
                        (when temp__5457__auto__
                          (let [df temp__5457__auto__] (str " (default " df ")"))))))
                  (recur seq_20674 chunk_20675 count_20676 (inc i_20677)))
                (let [temp__5457__auto__ (seq seq_20674)]
                  (when temp__5457__auto__
                    (let [seq_20674 temp__5457__auto__]
                      (if (chunked-seq? seq_20674)
                        (let [c__5719__auto__ (chunk-first seq_20674)]
                          (recur
                            (chunk-rest seq_20674)
                            c__5719__auto__
                            (int (count c__5719__auto__))
                            (int 0)))
                        (let [opt (first seq_20674)]
                          (println
                            (str
                              (format
                                "%-20s: "
                                (str
                                  (let [temp__5457__auto__ (:short-name opt)]
                                    (when temp__5457__auto__
                                      (let [sn temp__5457__auto__] (str "-" (name sn) ",\n "))))
                                  "--"
                                  (name (:long-name opt))))
                              (:doc opt)
                              (let [temp__5457__auto__ (:default opt)]
                                (when temp__5457__auto__
                                  (let [df temp__5457__auto__] (str " (default " df ")"))))))
                          (recur (next seq_20674) nil 0 0)))))))))
          (when (seq positional)
            (println "\nPositional arguments:")
            (let [idx (unique-index positional :long-name)]
              (loop [seq_20678 (seq positions) chunk_20679 nil count_20680 0 i_20681 0]
                (if (< i_20681 count_20680)
                  (let [pos (.nth ^clojure.lang.Indexed chunk_20679 (int i_20681))]
                    (let [temp__5457__auto__ (get idx pos)]
                      (when temp__5457__auto__
                        (let [arg temp__5457__auto__]
                          (println
                            (str
                              (format "%-20s: " (name (:long-name arg)))
                              (:doc arg)
                              (let [temp__5457__auto__ (:default arg)]
                                (when temp__5457__auto__
                                  (let [df temp__5457__auto__] (str " (default " df ")")))))))))
                    (recur seq_20678 chunk_20679 count_20680 (inc i_20681)))
                  (let [temp__5457__auto__ (seq seq_20678)]
                    (when temp__5457__auto__
                      (let [seq_20678 temp__5457__auto__]
                        (if (chunked-seq? seq_20678)
                          (let [c__5719__auto__ (chunk-first seq_20678)]
                            (recur
                              (chunk-rest seq_20678)
                              c__5719__auto__
                              (int (count c__5719__auto__))
                              (int 0)))
                          (let [pos (first seq_20678)]
                            (let [temp__5457__auto__ (get idx pos)]
                              (when temp__5457__auto__
                                (let [arg temp__5457__auto__]
                                  (println
                                    (str
                                      (format "%-20s: " (name (:long-name arg)))
                                      (:doc arg)
                                      (let [temp__5457__auto__ (:default arg)]
                                        (when temp__5457__auto__
                                          (let [df temp__5457__auto__]
                                            (str " (default " df ")")))))))))
                            (recur (next seq_20678) nil 0 0)))))))))
            nil)))))
  (defn cli->map
    ([strings positions vararg]
      (let [G__20698 strings
            vec__20699 G__20698
            seq__20700 (seq vec__20699)
            first__20701 (first seq__20700)
            seq__20700 (next seq__20700)
            s first__20701
            more seq__20700
            positions positions
            m {}]
        (loop [G__20698 G__20698 positions positions m m]
          (let [vec__20702 G__20698
                seq__20703 (seq vec__20702)
                first__20704 (first seq__20703)
                seq__20703 (next seq__20703)
                s first__20704
                more seq__20703
                positions positions
                m m]
            (if s
              (let [temp__5455__auto__ (re-matches #"-+(.*)" s)]
                (if temp__5455__auto__
                  (let [vec__20705 temp__5455__auto__
                        _ (nth vec__20705 (int 0) nil)
                        k (nth vec__20705 (int 1) nil)]
                    (if (= (keyword k) vararg)
                      (assoc m (keyword k) (into [s] more))
                      (let [vec__20708 more
                            seq__20709 (seq vec__20708)
                            first__20710 (first seq__20709)
                            seq__20709 (next seq__20709)
                            v first__20710
                            more seq__20709]
                        (recur more positions (assoc m (keyword k) v)))))
                  (let [vec__20711 positions
                        seq__20712 (seq vec__20711)
                        first__20713 (first seq__20712)
                        seq__20712 (next seq__20712)
                        k first__20713
                        pmore seq__20712]
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
          (fn fn__20718
            ([m p__20717]
              (let [vec__20719 p__20717
                    k (nth vec__20719 (int 0) nil)
                    v (nth vec__20719 (int 1) nil)]
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
      (let [map__20728 (apply sh/sh args)
            map__20728 (if (seq? map__20728)
                         (clojure.lang.PersistentHashMap/create (seq map__20728))
                         map__20728)
            m map__20728
            exit (get map__20728 :exit)
            err (get map__20728 :err)]
        (when-not (or (= 0 exit) (seq err))
          (throw (ex-info "Shell command failed" {:args args, :result m})))
        (:out m))))
  (defn development-version
    ([& _]
      (let [map__20731 (read-string (slurp "pom.clj"))
            map__20731 (if (seq? map__20731)
                         (clojure.lang.PersistentHashMap/create (seq map__20731))
                         map__20731)
            version_prefix (get map__20731 :version-prefix)
            revision (str/trimr (shell "build/revision"))]
        (str version_prefix "." revision)))))
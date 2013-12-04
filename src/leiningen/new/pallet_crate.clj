(ns leiningen.new.pallet-crate
  (:require
   [clojure.java.io :refer [file]]
   [clojure.string :refer [split]]
   [leiningen.core.main :refer [debug]]
   [leiningen.new.templates
    :refer [name-to-path project-name renderer sanitize sanitize-ns ->files]]))

(def render (renderer "pallet-crate"))

(defn file-str
  "Like java.io.file, but sanitizes its args and returns a string."
  [& args]
  (.getPath (apply file (map name-to-path args))))

(defn pallet-crate
  "Create a new pallet crate.

The name can be namespaced to specify the default groupId.
An optional namespace prefix may be specified."
  ([name ns-prefix]
     (let [project-name (project-name name)
           data {:name project-name
                 :kw (keyword project-name)
                 :ns (sanitize-ns (str ns-prefix "." project-name))
                 :sanitized (file-str ns-prefix project-name)}]
       (debug "data" data)
       (->files data
                ["src/{{sanitized}}.clj" (render "crate.clj" data)]
                ["test/{{sanitized}}_test.clj" (render "test.clj" data)]
                ["dev-resources/logback-test.xml" (render "logback-test.xml" data)]
                ["pallet.clj" (render "pallet.clj" data)]
                ["project.clj" (render "project.clj" data)]
                ["profiles.clj" (render "profiles.clj" data)]
                [".gitignore" (render "gitignore" data)]
                ["release.sh" (render "release.sh" data)]
                ["resources/pallet_crate/{{name}}_crate/meta.edn"
                 (render "meta.edn" data)]
                ["doc-src/USAGE.md" (render "USAGE.md" data)]
                ["doc-src/FOOTER.md" (render "FOOTER.md" data)])))
  ([name]
     (pallet-crate name (first (split name #"/")))))

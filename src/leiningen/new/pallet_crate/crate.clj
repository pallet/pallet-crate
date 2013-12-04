;; TODO edit this with links to the software being installed and configured
(ns {{ns}}
  "A [pallet](https://palletops.com/) crate to install and configure {{name}}"
  (:require
   [pallet.action :refer [with-action-options]]
   [pallet.actions :refer [directory exec-checked-script remote-directory
                           remote-file]
    :as actions]
   [pallet.api :refer [plan-fn] :as api]
   [pallet.crate :refer [assoc-settings defmethod-plan defplan get-settings]]
   [pallet.crate-install :as crate-install]
   [pallet.stevedore :refer [fragment]]
   [pallet.script.lib :refer [config-root file]]
   [pallet.utils :refer [apply-map deep-merge]]
   [pallet.version-dispatch :refer [defmethod-version-plan
                                    defmulti-version-plan]]
   [clojure.tools.logging :as log]))

;;; # Settings
(defn default-settings
  "Provides default settings, that are merged with any user supplied settings."
  []
  ;; TODO add configuration options here
  {:user "{{name}}"
   :group "{{name}}"
   :owner "{{name}}"
   :config-dir (fragment (file (config-root) "{{name}}"))})

(defmulti-version-plan settings-map [version settings])

(defmethod-version-plan
    settings-map {:os :linux}
    [os os-version version settings]
  (cond
   (:install-strategy settings) settings
   :else (assoc settings
           :install-strategy :packages
           :packages ["curl"])))


(defplan settings
  "Settings for {{name}}"
  [settings {:keys [instance-id] :as options}]
  (let [settings (deep-merge (default-settings) settings)
        settings (settings-map (:version settings) settings)]
    (assoc-settings {{kw}} settings {:instance-id instance-id})))

;;; # User
(defplan user
  "Create the {{name}} user"
  [{:keys [instance-id] :as options}]
  (let [{:keys [user owner group home]} (get-settings {{kw}} options)]
    (actions/group group :system true)
    (when (not= owner user)
      (actions/user owner :group group :system true))
    (actions/user
     user :group group :system true :create-home true :shell :bash)))

;;; # Install
(defplan install
  "Install {{name}}"
  [ {:keys [instance-id]}]
  (let [settings (get-settings {{kw}} {:instance-id instance-id})]
    (crate-install/install {{kw}} instance-id)))

;;; # Configure
(def ^{:doc "Flag for recognising changes to configuration"}
  {{name}}-config-changed-flag "{{name}}-config")

(defplan config-file
  "Helper to write config files"
  [{:keys [owner group config-dir] :as settings} filename file-source]
  (directory config-dir :owner owner :group group)
  (apply
   remote-file (str config-dir "/" filename)
   :flag-on-changed {{name}}-config-changed-flag
   :owner owner :group group
   (apply concat file-source)))

(defplan configure
  "Write all config files"
  [{:keys [instance-id config] :as options}]
  (let [{:keys [] :as settings} (get-settings {{kw}} options)]
    (config-file settings "{{name}}.conf" {:content (str config)})
    (exec-checked-script "download my buddy Isaac"
                         ("curl" "-o" "/tmp/my-file"
                          "http://en.wikipedia.org/wiki/Isaac_newton"))))

;;; # Server spec
(defn server-spec
  "Returns a server-spec that installs and configures {{name}}."
  [settings & {:keys [instance-id config] :as options}]
  (api/server-spec
   :phases
   {:settings (plan-fn
               ({{ns}}/settings settings options))
    :install (plan-fn
              (user options)
              (install options))
    :configure (plan-fn
                (configure options)
                ;;(run options)
                )}))

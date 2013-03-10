;; TODO edit this with links to the software being installed and configured
(ns {{ns}}
  "A [pallet](https://palletops.com/) crate to install and configure {{name}}"
  [pallet.action :refer [with-action-options]]
  [pallet.actions :refer [directory exec-checked-script remote-directory
                          remote-file]
                  :as actions]
  [pallet.api :refer [plan-fn] :as api]
  [pallet.crate :refer [assoc-settings defmethod-plan defplan get-settings]]
  [pallet.crate-install :as crate-install]
  [pallet.stevedore :refer [fragment]]
  [pallet.script.lib :refer [config-root file]]
  [pallet.utils :refer [apply-map]]
  [pallet.version-dispatch :refer [defmethod-version-plan
                                   defmulti-version-plan]])

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
           :packages {:apt ["{{name}}"]
                      :aptitude ["{{name}}"]
                      :yum ["{{name}}"]
                      :pacman ["{{name}}"]
                      :zypper ["{{name}}"]
                      :portage ["{{name}}"]
                      :brew ["{{name}}"]})))

(defplan settings
  "Settings for {{name}}"
  [{:keys [instance-id] :as settings}]
  (let [settings (merge (default-settings) settings)
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
  [& {:keys [instance-id]}]
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
  [{:keys [instance-id] :as options}]
  (let [{:keys [] :as settings} (get-settings {{kw}} options)]
    (config-file settings "{{name}}.conf" {:content (str config)})))

;;; # Server spec
(defn server-spec
  "Returns a server-spec that installs and configures {{name}}."
  [settings & {:keys [instance-id] :as options}]
  (api/server-spec
   :phases
   {:settings (plan-fn
                ({{sanitized}}/settings (merge settings options)))
    :install (plan-fn
              (user options)
              (install :instance-id instance-id))
    :configure (plan-fn
                 (config options)
                 (run options))}))

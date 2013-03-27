(ns {{ns}}-test
  (:require
   [{{ns}} :as {{name}}]
   [pallet.actions :refer [package-manager exec-script* remote-file]]
   [pallet.algo.fsmop :refer [complete?]]
   [pallet.api :refer [plan-fn server-spec]]
   [pallet.script-test :refer [testing-script is-true]]
   [pallet.crate.network-service :refer [wait-for-port-listen]]))

(def live-test-spec
  (server-spec
   :extends [({{name}}/server-spec {})]
   :phases {:install (plan-fn (package-manager :update))
            :test (plan-fn
                   (exec-script*
                    (testing-script "Checking Isaac downloaded"
                     (is-true
                      (file-exists? "/tmp/my-file")
                      "Isaac downloaded"))))}))
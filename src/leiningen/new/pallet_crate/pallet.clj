(require '[{{ns}}-test :refer [live-test-spec]]
         '[pallet.crates.test-nodes :refer [node-specs]])

(defproject {{name}}-crate
  :provider node-specs
  :groups [(group-spec "{{name}}-test"
             :extends [with-automated-admin-user live-test-spec]
             :roles #{:live-test :default :{{name}}})])
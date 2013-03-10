{:dev
 {:plugins [[lein-set-version "0.3.0"]]
  :test-selectors {:default (complement :live-test)
                   :live-test :live-test
                   :all (constantly true)}}
  :release
  {:set-version
   {:updates [{:path "README.md" :no-snapshot true}]}}}

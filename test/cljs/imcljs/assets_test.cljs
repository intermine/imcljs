(ns imcljs.assets-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]))

(def service {:root "www.flymine.org/query"
              :model {:name "genomic"}})


(deftest class-keys
  (testing "Able to retrieve class keys"
    (async done
      (go
        (let [response (<! (fetch/class-keys service))]
          (contains? response :BioEntity)
          (done))))))

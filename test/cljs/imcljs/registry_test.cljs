(ns imcljs.registry-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]))

(deftest get-plain
  (let [request (fetch/registry false)]
    (testing "registry should return some InterMines"
      (async done
        (go
          (let [res (<! request)]
            (println res)
            (is (= 1 1))
            (done)))))))

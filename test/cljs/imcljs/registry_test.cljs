(ns imcljs.registry-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]))

(deftest registry
  (let [prod-mines (fetch/registry false)
        dev-and-prod-mines (fetch/registry true)]
    (testing "registry should return some InterMines, but there should be fewer
              prod than prod+dev mines"
      (async done
        (go
          (let [prod (<! prod-mines)
                dev (<! dev-and-prod-mines)]
            (is (<
                 (count (:instances (:body prod)))
                 (count (:instances (:body dev)))))
            (done)))))))

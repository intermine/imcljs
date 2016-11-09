(ns imcljs.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [imcljs.core :as core]))

(deftest fake-test
  (testing "fake descriptions"
    (is (= 1 2))))
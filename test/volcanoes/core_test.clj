(ns volcanoes.core-test
  (:require [clojure.test :refer :all]
            [volcanoes.core :refer :all]))

(deftest transform-header-test
  (is (= :elevation-meters (transform-header "Elevation (m)")))
  (is (= :hello (transform-header "HEllo")))
  (is (= :ruby-tuesday (transform-header "Ruby Tuesday"))))

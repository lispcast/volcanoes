(ns volcanoes.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]))

;; Main ways to execute code in your files

;; 1. Whole file
;; 2. Top-level form (expression)
;; 3. Single expression

;; 4. REPL Prompt

;; Guideline

(def csv-lines
  (with-open [csv (io/reader "/Users/eric/Desktop/GVP_Volcano_List_Holocene.csv")]
    (doall
      (csv/read-csv csv))))

(defn transform-header [header]
  (if (= "Elevation (m)" header)
    :elevation-meters
    (-> header
      clojure.string/lower-case
      (clojure.string/replace #" " "-")
      keyword)))

(defn transform-header-row [header-line]
  (map transform-header header-line))

(def volcano-records
  (let [csv-lines (rest csv-lines)
        header-line (transform-header-row (first csv-lines))
        volcano-lines (rest csv-lines)]
    (map (fn [volcano-line]
           (zipmap header-line volcano-line))
      volcano-lines)))

(defn parse-eruption-date [date]
  (if (= "Unknown" date)
    nil
    (let [[_ y e] (re-matches #"(\d+) (.+)" date)]
      (cond
        (= e "BCE")
        (- (Integer/parseInt y))
        (= e "CE")
        (Integer/parseInt y)
        :else
        (throw (ex-info "Could not parse year." {:year date}))))))

(defn parse-numbers [volcano]
  (-> volcano
    (update :elevation-meters #(Integer/parseInt %))
    (update :longitude #(Double/parseDouble %))
    (update :latitude #(Double/parseDouble %))
    (assoc :last-eruption-parsed (parse-eruption-date (:last-known-eruption volcano)))))

(def volcanoes-parsed
  (mapv parse-numbers volcano-records))

(def types (set (map :primary-volcano-type volcano-records)))

#_(println "All done!")

(comment

  ;; REPL-driven code

  (let [volcano (nth volcanoes-parsed 100)]
    (clojure.pprint/pprint volcano))

  (let [volcano (first (filter #(= "221291" (:volcano-number %)) volcanoes-parsed))]
    (clojure.pprint/pprint volcano))

  )



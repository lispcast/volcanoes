(ns volcanoes.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.string   :as str]))

;; Main ways to execute code in your files

;; 1. Whole file
;; 2. Top-level form (expression)
;; 3. Single expression

;; 4. REPL Prompt

;; Ergonomics

;; 1. Autocomplete
;;    unintrusive <-> intrusive
;; 2. Brace matching
;;   2.1 rainbow parens
;;   2.2 highlight matching
;; 3. Visual prompts
;;   args

;; Structural Editing
;; 1. Nothing
;; 2. Simple
;; 3. Parinfer
;; 4. Paredit

;; Know your printers
;; 1. println
;; 2. prn
;; 3. pprint

;; def trick for storing values for use at the repl

(defn p [v & more]
  (apply prn v more)
  v)

;; Tricks
;; 1. doto
;; 2. define new print function
;; 3. _ (in a let)

;; Scientific Debugging
;; 1. Testing individual expressions to locate the problem
;; 2. Set up env using def
;; 3. Inside->out, outside->in

(def csv-lines
  (with-open [csv (io/reader "/Users/eric/Desktop/GVP_Volcano_List_Holocene.csv")]
    (doall
     (csv/read-csv csv))))


(defn transform-header [header]
  (if (= "Elevation (m)" header)
    (do
      (throw (ex-info "Bug!" {}))
     :elevation-meters)
    (-> header
        (clojure.string/replace #"L" "l")
        clojure.string/lower-case
        (clojure.string/replace #" " "-")
        keyword)))

(defn transform-header-row [header-line]
  (map transform-header header-line))

(def volcano-records
  (let [data-csv-lines (rest csv-lines)
        header-line (transform-header-row (first data-csv-lines))
        volcano-lines (rest data-csv-lines)]
    (map (fn [volcano-line]
           (zipmap header-line volcano-line))
         volcano-lines)))



(defn parse-eruption-date [date]
  (if (= "Unknown" date)
    nil
    (let [[_ y e] (re-matches #"(\d+)[\s]+(.+)" date)]
      (cond
        (= e "BCE")
        (- (Integer/parseInt y))
        (= e "CE")
        (Integer/parseInt y)
        :else
        (throw (ex-info "Could not parse year." {:year date}))))))

(defn slash->set [s]
  (set (map str/trim (str/split s #"/"))))

(defn parse-numbers [volcano]
  (-> volcano
      (update :elevation-meters #(Integer/parseInt %))
      (update :longitude #(Double/parseDouble %))
      (update :latitude #(Double/parseDouble %))
      (assoc :last-eruption-parsed (parse-eruption-date (:last-known-eruption volcano)))
      (update :tectonic-setting slash->set)
      (update :dominant-rock-type slash->set)))

(def volcanoes-parsed
  (mapv parse-numbers volcano-records))

(def types (set (map :primary-volcano-type volcano-records)))

#_(println "All done!")

(comment

  ;; REPL-driven code

  (let [volcano (nth volcanoes-parsed 100)]
    (clojure.pprint/pprint volcano))

  (let [volcano (first (filter #(= "221291" (:volcano-number %)) volcanoes-parsed))]
    (clojure.pprint/pprint volcano)))

(comment

  (parse-eruption-date "2014 CE")
  (parse-eruption-date "187  BCE"))



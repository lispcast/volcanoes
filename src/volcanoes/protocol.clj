(ns volcanoes.protocol)

(defprotocol Volcano
  (erupt [v])
  (measure [v]))

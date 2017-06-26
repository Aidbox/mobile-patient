(ns mobile-patient.lib.ajax
  (:require [cljs.core.async :as a])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defn ajax [method
            url
            opts]
  (let [ch (a/chan 1)]
    (-> (js/fetch url
                  (clj->js (merge {:method method
                                   :headers {"content-type" "application/json"}}
                                  opts)))
         (.then #(.json %))
         (.then
          (fn [response]
            (go (a/>! ch [:response (js->clj response :keywordize-keys true)])
                (print (js->clj response :keywordize-keys true))
                (a/close! ch))))
         (.catch
          (fn [error]
            (go (a/>! ch [:error (js->clj error :keywordize-keys true)])
                (print (js->clj error :keywordize-keys true))
                (a/close! ch)))))
    ch))

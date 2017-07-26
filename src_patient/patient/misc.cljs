(ns patient.misc
  (:require [re-frame.core :refer [subscribe]]
            [mobile-patient.lib.helper :as h]))

(defn setup-temperature [{:keys date value}]
  (let [patient-id @(subscribe [:get-in [:user :ref :id]])
        base-url @(subscribe [:get-in [:config :base-url]])
        data (h/vital-observation-data-wrapper patient-id
                                               "8310-5"
                                               date
                                               {:Quantity {:code "Cel" :value value}})
        ]
    (js/fetch (str base-url "/Observation")
              (clj->js {:method "POST"
                        :headers {"Content-Type" "application/json"}
                        :body (.stringify js/JSON (clj->js data))}))))



(comment
  (map setup-temperature
       [{:date "2017-07-01T00:00:00.0Z"
         :value 36.6
         }
         {:date "2017-07-02T00:00:00.0Z"
         :value 36.8
         }
         {:date "2017-07-03T00:00:00.0Z"
         :value 36.3
         }
         {:date "2017-07-04T00:00:00.0Z"
         :value 36.9
         }
         {:date "2017-07-05T00:00:00.0Z"
         :value 37.3
         }
         {:date "2017-07-06T00:00:00.0Z"
         :value 38.3
         }
         {:date "2017-07-07T00:00:00.0Z"
         :value 39.5
         }])







  (-> (setup-vitals

       )
      (.then #(.text %))
      (.then #(println "Result" %))
      (.catch #(println "Fetch error" (.-message %))))


  ;; @(subscribe [:get-in [:user :ref :id]])

  )

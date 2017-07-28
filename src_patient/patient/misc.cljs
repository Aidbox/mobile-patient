(ns patient.misc
  (:require [re-frame.core :refer [subscribe]]
            [mobile-patient.lib.helper :as h]))

(defn setup-vitals [{:keys [code date value]}]
  (let [patient-id @(subscribe [:get-in [:user :ref :id]])
        base-url @(subscribe [:get-in [:config :base-url]])
        data (h/vital-observation-data-wrapper patient-id
                                               code
                                               date
                                               value)
        ]
    (js/fetch (str base-url "/Observation")
              (clj->js {:method "POST"
                        :headers {"Content-Type" "application/json"}
                        :body (.stringify js/JSON (clj->js data))}))))

(defn cleanup-vitals []
  (let [patient-id @(subscribe [:get-in [:user :ref :id]])
        base-url @(subscribe [:get-in [:config :base-url]])]
        (-> (js/fetch (str base-url (str "/Observation?patient=" patient-id)))
            (.then #(.json %))
            (.then #(js->clj % :keywordize-keys true))
            (.then (fn [data]
                     (->> data
                       (:entry)
                       (map #(get-in % [:resource :id]))
                       (map (fn [id]
                              (println id)
                              (-> (js/fetch (str base-url "/Observation/" id) (clj->js {:method "DELETE"}))
                                  (.then #(.text %))
                                  (.then #(println "Result" %))
                                  (.catch #(println "Fetch error" (.-message %))))))
                       (println)))))))




(comment

  (let [temperature [{:date "2017-07-01T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 36.6}}
                      }
                     {:date "2017-07-02T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 36.7}}
                     }
                     {:date "2017-07-03T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 36.9}}
                     }
                     {:date "2017-07-04T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 37.1}}
                     }
                     {:date "2017-07-05T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 36.6}}
                     }
                     {:date "2017-07-06T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 36.7}}
                     }
                     {:date "2017-07-07T00:00:00.0Z"
                      :code "8310-5"
                      :value {:Quantity {:code "Cel" :value 37.7}}
                      }]
        blood-pressure [{:date "2017-07-01T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 63 :code "mmHg"} :high {:value 109 :code "mmHg"}}}
                        }
                        {:date "2017-07-02T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 70 :code "mmHg"} :high {:value 110 :code "mmHg"}}}
                        }
                        {:date "2017-07-03T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 61 :code "mmHg"} :high {:value 117 :code "mmHg"}}}
                        }
                        {:date "2017-07-04T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 62 :code "mmHg"} :high {:value 100 :code "mmHg"}}}
                        }
                        {:date "2017-07-05T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 59 :code "mmHg"} :high {:value 120 :code "mmHg"}}}
                        }
                        {:date "2017-07-06T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 66 :code "mmHg"} :high {:value 103 :code "mmHg"}}}
                        }
                        {:date "2017-07-07T00:00:00.0Z"
                         :code "85354-9"
                         :value {:Range {:low {:value 65 :code "mmHg"} :high {:value 101 :code "mmHg"}}}
                         }]
        respiratory-rate [{:date "2017-07-01T00:00:00.0Z"
                           :code "9279-1"
                           :value {:Quantity {:code "/min" :value 26}}
                           }
                           {:date "2017-07-02T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 27}}
                           }
                           {:date "2017-07-03T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 28}}
                           }
                           {:date "2017-07-04T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 29}}
                           }
                           {:date "2017-07-05T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 22}}
                           }
                           {:date "2017-07-06T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 21}}
                           }
                           {:date "2017-07-07T00:00:00.0Z"
                            :code "9279-1"
                            :value {:Quantity {:code "/min" :value 26}}
                           }]
        heart-rate [{:date "2017-07-01T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 126}}
                    }
                    {:date "2017-07-02T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 120}}
                    }
                    {:date "2017-07-03T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 125}}
                    }
                    {:date "2017-07-04T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 121}}
                    }
                    {:date "2017-07-05T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 122}}
                    }
                    {:date "2017-07-06T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 123}}
                    }
                    {:date "2017-07-07T00:00:00.0Z"
                     :code "8867-4"
                     :value {:Quantity {:code "/min" :value 100}}
                    }]]
    (cleanup-vitals)
    (map setup-vitals (concat blood-pressure temperature heart-rate respiratory-rate))
    )
  )

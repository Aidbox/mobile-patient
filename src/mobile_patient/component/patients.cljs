(ns mobile-patient.component.patients
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.component.person-row :refer [person-row-component]]))

(defn Patients [data navigation on-press icon-name]
  [ui/flat-list {:style {:background-color :white}
                 :data (clj->js data)
                 :key-extractor #(.-id %)
                 :render-item (fn [row]
                                (r/as-element [person-row-component (js->clj row :keywordize-keys true) navigation on-press icon-name]))}])

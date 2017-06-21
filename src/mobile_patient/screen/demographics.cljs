(ns mobile-patient.screen.demographics
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))



(defn submit-handler [age sex address dispatch]
  (if (or (empty? age)
          (empty? sex)
          (empty? address))
    (ui/alert "" "All fields are required")
    (dispatch nav/tabs)
    ))


(defn DemographicsScreen [{:keys [navigation]}]
  (let [age (atom "")
        sex (r/atom "")
        address (atom "")]
    (fn []
      [ui/view {:style {:flex 1}}

       [ui/text "Age"]
       [ui/input {:on-change-text #(reset! age %)
                  :keyboard-type :numeric}]

       [ui/text "Sex"]
       [ui/picker {:on-value-change #(reset! sex %)
                   :selected-value @sex}
        [ui/picker-item {:label "-- Select Gender --" :value ""}]
        [ui/picker-item {:label "Male" :value "male"}]
        [ui/picker-item {:label "Female" :value "female"}]]

       [ui/text "Address"]
       [ui/input {:on-change-text #(reset! address %)}]

       [ui/button {:title "Save" :on-press #(submit-handler @age
                                                            @sex
                                                            @address
                                                            navigation.dispatch)}]])))

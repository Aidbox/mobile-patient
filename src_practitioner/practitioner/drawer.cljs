(ns practitioner.drawer
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

(defn Item
  ([title on-press]
   (Item title on-press nil))
  ([title on-press style-props]
   [ui/touchable-highlight {:style (merge {:height 45
                                           :justify-content :center}
                                          style-props)
                            :on-press on-press
                            :underlay-color color/grey}
    [ui/text {:style {:margin-left 50
                      :font-size 24}} title]]))

(defn Drawer [props]
  (let [username @(subscribe [:get-in [:user :name 0 :text]])
        patient @(subscribe [:get-in [:patient]])]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 100
                       :flex-direction :row
                       :justify-content :center}}
      [ui/view {:style {:width 48
                        :height 48
                        :border-radius 24
                        :background-color "#9e9e9e"
                        :align-self :center}}]
      [ui/text {:style {:font-size 20
                        :margin-left 10
                        :color "black"
                        :align-self :center}} username]]

     [ui/view {:style {:flex 1
                       :margin-top 30}}
      [Item "Patients" #(props.navigation.navigate "Patients")]

      ;; patients's specific items
      (when @(subscribe [:patient-ref])
        [ui/view
         [ui/text {:style {:margin-left 50 :margin-top 20 :font-size 24 :color :black}}
           (str @(subscribe [:get-in [:patient-data :id]])  "'s:")]
         [Item "Medications" #(props.navigation.navigate "Medications")]
         [Item "Vitals Signs" #(props.navigation.navigate "Vitals Signs")]
         [Item "Chats" #(props.navigation.navigate "Chats")]
         ])
      ]
     [Item "Logout" #(dispatch [:initialize-db])]
     [Item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]
     ])
  )

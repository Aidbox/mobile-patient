(ns patient.drawer
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

(defn Item
  ([title on-press]
   (Item title on-press nil))
  ([title on-press style-props]
   [ui/touchable-highlight {:style (merge {:height 40
                                           :justify-content :center}
                                          style-props)
                            :on-press on-press
                            :underlay-color color/grey}
    [ui/text {:style {:margin-left 40
                      :font-size 18}} title]]))

(defn Drawer [props]
  (let [username @(subscribe [:get-in [:user :id]])
        excluded #{"About App" "Patients"}]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 100
                       :flex-direction :row
                       :justify-content :flex-start
                       :padding-left 30}}
      [ui/view {:style {:width 38
                        :height 38
                        :border-radius 19
                        :background-color "#9e9e9e"
                        :align-self :center}}]
      [ui/text {:style {:font-size 18
                        :margin-left 20
                        :color "#333"
                        :align-self :center}} username]]
     [ui/view {:style {:flex 1
                       :margin-top 30}}
      (for [route (-> props .-items) :when (not (excluded (.-key route)))]
        ^{:key (.-key route)}
        [Item (.-routeName route) #(props.navigation.navigate (.-key route))])
      [Item "Logout" #(do
                               (dispatch [:initialize-db])
                               (dispatch [:set-current-screen :login]))]]
     [Item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]]))

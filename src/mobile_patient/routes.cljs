(ns mobile-patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [mobile-patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.screen.chat :refer [ChatsScreen ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]
            [mobile-patient.screen.chart :refer [ChartScreen]]))


(def react-navigation (js/require "react-navigation"))

(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))
(def DrawerNavigator (.-DrawerNavigator react-navigation))

;; NAVIGATION BUTTONS

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")
                           :style {:margin-right 20}
                           :underlay-color color/grey}
   [ui/icon {:name "add-circle" :size 36 :color color/pink}]])

;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "DrawerOpen")
                           :underlay-color color/grey}
   [ui/icon {:name "menu" :size 36 :color color/grey}]])

(defn drawer-item
  ([title on-press]
   (drawer-item title on-press nil))
  ([title on-press style-props]
   [ui/touchable-highlight {:style (merge {:height 45
                                           :justify-content :center}
                                          style-props)
                            :on-press on-press
                            :underlay-color color/grey}
    [ui/text {:style {:margin-left 50
                      :font-size 24}} title]]))

(defn drawer-content [props]
  (let [user-id @(rf/subscribe [:user-id])
        excluded #{"About App"}]
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
                        :align-self :center}} user-id]]
     [ui/view {:style {:flex 1
                       :margin-top 30}}
      (for [route (-> props .-items) :when (not (excluded (.-key route)))]
        ^{:key (.-key route)}
        [drawer-item (.-routeName route) #(props.navigation.navigate (.-key route))])
      [drawer-item "Logout" #(do
                               (rf/dispatch [:initialize-db])
                               (rf/dispatch [:set-current-screen :login]))]]
     [drawer-item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]]))

(defn stack-navigator [routes]
  (r/reactify-component
   (StackNavigator (clj->js routes)
                   (clj->js {:headerTitleStyle {:fontWeight :normal}}))))

(defn stack-navigator-back-button [props]
  (r/as-element
   [ui/touchable-highlight {:on-press #(props.navigation.goBack nil)
                            :style {:margin-left 15}
                            :underlay-color color/grey}
    [ui/icon {:name "chevron-left" :size 36 :color color/pink}]]))

(defn drawer-nav-opts
  ([title]
   (drawer-nav-opts title nil))
  ([title header-right]
   (fn [props]
     #js{:title title
         :headerLeft (r/as-element [menu-button props.navigation])
         :headerTitleStyle #js{:fontWeight "normal"
                               :color "#6e6e6e"}
         :headerRight (when header-right (r/as-element [header-right props.navigation]))})))

(def drawer-routes
  (DrawerNavigator
   (clj->js
    {"Medications" {:screen (stack-navigator
                             {"Meds" {:screen (r/reactify-component MedsScreen)
                                      :navigationOptions (drawer-nav-opts "Medications")}})}

     "Vitals Signs" {:screen (stack-navigator
                              {"Vitals" {:screen (r/reactify-component VitalsScreen)
                                         :navigationOptions (drawer-nav-opts "Vitals Signs")}

                               "ExampleChart" {:screen (r/reactify-component ChartScreen)
                                               :navigationOptions (fn [props]
                                                                    #js{:title "Example Chart"
                                                                        :headerLeft (stack-navigator-back-button props)
                                                                        :headerTitleStyle #js{:fontWeight "normal"
                                                                                              :color "#6e6e6e"}})}})}

     "Chats" {:screen (stack-navigator
                       {"Chats" {:screen (r/reactify-component ChatsScreen)
                                 :navigationOptions (drawer-nav-opts "Chats" add-chat-button)}

                        "Chat" {:screen (r/reactify-component ChatScreen)
                                :navigationOptions
                                (fn [props]
                                  (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                                    #js {:title chat-name
                                         :headerLeft (stack-navigator-back-button props)
                                         :headerTitleStyle #js{:fontWeight "normal"
                                                               :color "#6e6e6e"}}))}

                        "Contacts" {:screen (r/reactify-component ContactsScreen)
                                    :navigationOptions
                                    (fn [props]
                                      #js{:title "Contacts"
                                          :headerLeft (stack-navigator-back-button props)
                                          :headerTitleStyle #js{:fontWeight "normal"
                                                                :color "#6e6e6e"}})}})}

     "Settings" {:screen (stack-navigator
                          {"Settings" {:screen (r/reactify-component SettingsScreen)
                                       :navigationOptions (drawer-nav-opts "Settings")}})}

     "About App" {:screen (stack-navigator
                           {"About App" {:screen (r/reactify-component (fn [] [ui/text "About app"]))
                                         :navigationOptions (drawer-nav-opts "About App")}})}})
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [drawer-content props]))})))

;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)]
    (case route-name
      "Vitals Signs" (rf/dispatch [:on-vitals-sign-screen])
      nil)))

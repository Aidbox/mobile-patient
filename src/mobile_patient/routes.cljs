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
            [mobile-patient.screen.contacts :refer [ContactsScreen]]))


(def react-navigation (js/require "react-navigation"))

(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))
(def DrawerNavigator (.-DrawerNavigator react-navigation))
(def DrawerItems (.-DrawerItems react-navigation))

;; NAVIGATION BUTTONS

(defn logout-button [navigation]
  [ui/link {:title "Log Out"
            :on-press #(do
                         (rf/dispatch [:initialize-db])
                         (rf/dispatch [:set-current-screen :login]))}])

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")
                           :style {:margin-right 20}}
   [ui/icon {:name "add-circle" :size 36 :color color/pink}]])

(defn settings-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Settings")}
   [ui/icon {:name "settings" :size 36 :color color/grey :margin-right 8}]])

(defn chat-buttons [navigation]
  [ui/view {:flex-direction :row :justify-content :space-around :width 86}
   [settings-button navigation]
   [add-chat-button navigation]])


;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 10}
                           :on-press #(navigation.navigate "DrawerOpen")}
   [ui/icon {:name "menu" :size 36 :color color/grey}]])

(defn drawer-nav-button []
  [ui/touchable-opacity
   [ui/view {:style {:flexDirection :row
                     :height 50
                     :paddingLeft 15
                     :backgroundColor "#fff0"
                     :borderTopWidth 0.5
                     :borderColor "#fff"}}
    [ui/text {:style {:fontSize 36
                      :color "#fff"}} "Name"]]])

(defonce props-tmp (atom nil))

(defn drawer-content-comp [props]
  (let [user-id @(rf/subscribe [:user-id])
        filter-items (fn [props]
                       (let [clj-props (js->clj props)
                             excluded #{"About App"}]
                         (clj->js
                          (merge clj-props
                                 {"items" (remove #(excluded (% "key"))
                                                  (clj-props "items"))}))))]
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
     [ui/view {:flex 1}
      [DrawerItems (filter-items props)]
      [ui/touchable-highlight {:style {:height 45
                                       :justify-content :center}
                               :on-press #(do
                                            (rf/dispatch [:initialize-db])
                                            (rf/dispatch [:set-current-screen :login]))
                               :underlay-color color/grey}
       [ui/text {:style {:margin-left 50
                         :font-size 24}} "Logout"]]]
     [ui/touchable-highlight {:style {:margin-bottom 60
                                      :height 45
                                      :justify-content :center}
                              :underlay-color color/grey
                              :on-press #(props.navigation.navigate "About App")}
      [ui/text {:style {:margin-left 50
                        :font-size 24}} "About app"]]]))

(defn stack-navigator
  ([routes]
   (stack-navigator routes {}))
  ([routes nav-opts]
   (r/reactify-component
    (StackNavigator (clj->js routes) (clj->js nav-opts)))))

(defn drawer-nav-opts [title header-right]
  (fn [props]
    #js{:title title
        :headerLeft (r/as-element [menu-button props.navigation])
        :headerRight (r/as-element [header-right props.navigation])}))

(def drawer-routes
  (DrawerNavigator
   (clj->js
    {"Medications" {:screen (stack-navigator
                             {"Meds" {:screen (r/reactify-component MedsScreen)
                                      :navigationOptions (drawer-nav-opts "Medications" logout-button)}})}

     "Vitals Signs" {:screen (stack-navigator
                        {"Vitals" {:screen (r/reactify-component VitalsScreen)
                                   :navigationOptions (drawer-nav-opts "Vitals Signs" logout-button)}})}

     "Chats" {:screen (stack-navigator
                       {"Chats" {:screen (r/reactify-component ChatsScreen)
                                 :navigationOptions (drawer-nav-opts "Chats" add-chat-button)}

                        "Settings" {:screen (r/reactify-component SettingsScreen)
                                    :navigationOptions {:title "Settings"}}

                        "Chat" {:screen (r/reactify-component ChatScreen)
                                :navigationOptions
                                (fn [props]
                                  (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                                    #js {:title chat-name}))}

                        "Contacts" {:screen (r/reactify-component ContactsScreen)
                                    :navigationOptions {:title "Contacts"}}
                        })}

     "Settings" {:screen (stack-navigator
                          {"Settings" {:screen (r/reactify-component SettingsScreen)
                                       :navigationOptions (drawer-nav-opts "Settings" logout-button)}})}

     "About App" {:screen (stack-navigator
                           {"About App" {:screen (r/reactify-component (fn [] [ui/text "About app"]))
                                         :navigationOptions (drawer-nav-opts "About App" logout-button)}})}
     })
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [drawer-content-comp props]))
     :contentOptions {:style {:marginVertical 0}
                      :labelStyle {:fontSize 24
                                   :marginLeft 50
                                   :marginVertical 8
                                   :fontWeight :normal}}})))

(def tab-routes
  (TabNavigator
   (clj->js
    {"Vitals" {:screen (r/reactify-component VitalsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Vitals"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Meds"   {:screen (r/reactify-component MedsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Meds"
                                         :headerRight (r/as-element [logout-button props.navigation])})}

     "Chats"   {:screen (r/reactify-component ChatsScreen)
               :navigationOptions (fn [props]
                                    #js {:title "Chats"
                                         :headerRight (r/as-element [chat-buttons props.navigation])})}})
   (clj->js
    {:tabBarPosition "bottom"
     :tabBarOptions {:activeTintColor "#fff"
                     :inactiveTintColor color/grey
                     :style {:backgroundColor color/light-grey}
                     :tabStyle {:backgroundColor color/grey}
                     :labelStyle {}}
     :initialRouteName "Chats" })))      ;delete

;; (def routes
;;   (StackNavigator
;;    (clj->js
;;     {"Tabs" {:screen tab-routes}

;;      "Drawer" {:screen drawer-routes}

;;      "Settings" {:screen (r/reactify-component SettingsScreen)
;;                  :headerTintColor color/grey
;;                  :navigationOptions {:title "Settings"}}

;;      "Chat" {:screen (r/reactify-component ChatScreen)
;;              :navigationOptions
;;              (fn [props]
;;                (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
;;                  #js {:title chat-name}))}

;;      "Contacts" {:screen (r/reactify-component ContactsScreen)
;;                  :navigationOptions {:title "Contacts"}}})
;;    (clj->js
;;     {:initialRouteName "Drawer"
;;      :navigationOptions {:headerTintColor color/grey}
;;      :headerMode :screen})))

(def routes drawer-routes)

# Notification Panel
This is a RuneLite plugin that displays notifications in an overlay panel. It is much more useful than native notifications when using multiple clients, and it's OS-agnostic.

![image demo](https://user-images.githubusercontent.com/87504405/180604834-a8cd83af-46b8-4095-abf9-74632a4aba24.png)

## Features / Options

* The notification panel can be repositioned and locked to anchors like any other overlay panel.

* The width of the notifications can be adjusted by alt-clicking on a border and dragging.

* The maximum number of notifications shown at once can range from 1 to 5.

* The expiration time can be shown or hidden.

* The duration each notification lasts can be set in units of seconds or ticks. Setting the duration to 0 will make notifications last forever (or until they are replaced by newer notifications). In this case, the "show time" setting will show the age of the notification.

* The font can be adjusted between "small," "regular," and "bold."

* Shift-right clicking a notification will show a "Clear" option which will clear all notifications.

* The following attributes can be set for all notifications matching a provided regex pattern:
  * Background color ("#ff0000")
  * Opacity ("opacity x" where x is an integer from 0 to 100)
  * Visibility ("hide" or "show")

    Each notification can match multiple regex lines, which can set different attributes.
    If multiple regex matches try to change the same attribute, the first match will take priority.
## Video Demo

https://user-images.githubusercontent.com/87504405/180604701-3876d03f-e058-418c-a545-199b737b8293.mp4

## TODO

* Bug testing
* Right-click option to clear individual notifications

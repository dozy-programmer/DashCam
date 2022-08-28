# DashCam

This project is my own implementation of a dashcam that leverages my limited knowledge of hardware and software. All in all, it would have been cheaper to just buy one from Amazon, but I don’t regret spending the extra money and the many hours debugging. This was a challenging project due to the fact that so variables had to be taken into consideration to ensure it would work properly. Solutions I thought were going to work, just flat out failed. Here are some of the problems I encountered:
-	Heat sensor to detect driver to know when to turn off Raspberry Pi failed because the range was too limited.
-	Utilized a portable battery so that when the car turns off, the dashcam is running off the portable battery, but this failed because there is a split second where it switches from car power to only battery resulting in the Raspberry Pi turning off.
-	Attempted to send live feed of front and back cameras to android app, but due to software/hardware limitations, the cameras could not be used concurrently to send live feed and facial recognition on the front camera.
-	And much more...

Given these limitations, I decided that the auto-turnoff feature of the Raspberry Pi was out of the scope of this project and that sending live feed of both cameras was unnecessary. The heat sensor was repurposed to give live heat measurement around the dashcam, which is continuously updated to Android App every 3-4 seconds. 

Project Scope:
This purpose of this project is to create a custom implementation of a dashcam that utilities two cameras. The first camera is facing the driver and its purpose is to determine if the owner (picture of owner is sent to Raspberry Pi via android app on initial setup) is driving the car. The front facing camera saves footage just like a regular dashcam and saved footage is neatly saved in file directory for easy viewing in the future.

I decided to implement the front facing camera after learning that "Of the 180,939 vehicles stolen
statewide in 2020, 89.2 percent were successfully recovered, representing 161,464 recovered vehicles" from the "2020 California Vehicle Theft Facts" report made by the California Highway patrol (CHP). Given that statistic, I think that the front camera can be useful after recovery of a vehicle since it has the ability to save other detected faces.

The Raspberry Pi 3 can be connected to an Android app I made in order to help in initializing owner data and send data/photo(s) via wifi-Sockets. A lot of time was spent to make sure that the connection is very stable and that data can be spent back and forth easily and accurately. 


## Table of contents
* [Materials](#materials)
* [App Layout](#app-layout)
* [Video Demo](#video-demo)
* [Features](#features)
* [Tools](#tools)
* [Download](#download)

## Materials
* Raspberry Pi 3
* Case for Raspberry Pi 3 + Fan
* USB Camera
* Raspberry Camera Module 2
* Portable Battery
* Heat Sensor

[TO DO] --> [Add Photos of Project]

## App Layout

No Connection | Setting Up | Take Selfie | Sending Photo | Image Received | Dashboard | Settings Page
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:
<img src="ScreenShots/Regular%20Device/waiting_for_connection_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> | <img src="ScreenShots/Regular%20Device/Initial_set_up_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> | <img src="ScreenShots/Regular%20Device/device_connected_for_setup_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> | <img src="ScreenShots/Regular%20Device/sending_photo_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> |<img src="ScreenShots/Regular%20Device/image_received_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> |<img src="ScreenShots/Regular%20Device/device_connected_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> |<img src="ScreenShots/Regular%20Device/settings_page_oneplus-oneplus8pro-portrait.png" width="350" height="350"/> 

## Video Demo

[TO DO] --> [Add video demo of project]

## Features
* No Connection:
	* current view when the the Android App is not connected to the Raspberry Pi.
* Settings Up:
	* When first connected and there has been no photos send, the set-up screen comes up.
* Take Selfie:
	* User has to take a photo to send to Raspberry Pi.
	* This photo is going to be the "Owner" photo that will be used to detect if the owner is driving the car via facial recognition.
* Sending Photo:
	* Sends the image taken and returns a message to verify the file was correctly sent over.
* Image Receieved:
	* Message states whether the photo was received and if the photo contains a face as required.
* Dashboard:
	* This shows the Raspberry Pi device name, it's IP Address, and returns the current temperature continuously every 4 seconds from the Raspberry Pi heat sensor.
* Settings Page:
	* This shows the owner name, the device IP Address (which can be edited), and the image(s) of the owner that were sent.
	
## Tools
* Android Studio
* Java
* Python
* Raspberry Pi 3

## Download
[![Website](https://img.shields.io/badge/DashCam-Download-orange)](https://github.com/Amark18/DashCam/blob/main/dashcam.apk)

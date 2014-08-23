Android-AVR
===========

Zack Brandes  
zack.brandes@gmail.com   
8/23/2014   

https://www.youtube.com/watch?v=yCX6QGX9yBk&feature=youtu.be

This repository contains three elements: 

1.		Android code to set up a server socket and send RGB values to the client.  
2.		A python script that sets up the client side socket on the computer to read in the RGB values  
			and send them to an AVR micro-controller via USART serial communication.  
3.		A C program written for an atmega328 that receives USART data in order to modulate PWM duty cycles and control and RGB LED.

IT IS IMPORTANT THAT YOU READ THESE INSTRUCTIONS CAREFULLY  

NOTE: The Android server socket code was taken heavily from Alec Florescu's website. Thank you Alex.  
http://www.florescu.org/archives/2010/10/15/android-usb-connection-to-pc/comment-page-1/  

RGB LED:  
The AVR code works for a common anode RGB LED wired to an atmega328 as following:  
	Red LED to PD5  
	Blue LED to PD6  
	Green LED to PB5  
Refer to the atmega328 pinout for clarification.  
If you are using the a common cathode RGB LED, then you will need to edit the while loop of the AVR code, 
	otherwise you will get opposite results.

ANDROID APP:  
1. Create a new android app and use the MainActivity.java and activity_main.xml files in the repo.  
2. Update the android manifest file with   

    <uses-permission android:name="android.permission.INTERNET"/> under the <manifest> tag.  
	
3. Upload the app to your device.  

AVR Code:  
The code was written and uploaded using AtmelStudio, however it should work in the Arduino IDE.  
1. Upload the code.  

PYTHON SCRIPT  
1. Before the script can be run, the android device must be connected to the computer and USB debugging mode must be enabled.  
2. Open a cmd prompt and execute the following line  

	"PATH_TO_ADB"/adb.exe forward tcp:38300 tcp:38300  
	
	This forwards a port on the computer to the device, allowing a socket communication to be set up on localhost.  
	If at any time your device becomes unplugged, the above line must be executed again.  
3. Make sure there are no open serial interfaces with the micro-controller/arduino.  
4. Open the Android app, hit connect, and then run the python script.  
   A message should pop up on the app if the connection was successful, or if the connection timed out.  
5.	You should now be able to modulate the RGB LED using the Android app.  
	


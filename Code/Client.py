import socket
import serial

# Client.py
# Author: Zack Brandes

#################################################################################
# This python script simply sets up a client side socket communication          #
# on port 38300, as well as a serial communication with the avr microcontroller.#
# The received bytes on the socket are sent directly over                       #
# the serial communication. FOLLOW INSTRUCTIONS BELOW                           #
#################################################################################

# 1. Make sure your android device is plugged into the computer and USB debugging is enabled
# 2. Open a windows cmd prompt and execute: "PATH-TO-ADB\adb.exe" forward tcp:38300 tcp:38300 
# 3. Press connect on the Android app, and immediately execute this script
# 4. If an error occurs, check your COM Port, verify step 2 was performed correctly, try again.

HOST = 'localhost'  
PORT = 38300
COM = 'COM9'

connected = 1
print('Attempting to connect')
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((HOST,PORT))
except ConnectionRefusedError:
    print("Socket connection refused")
    connected = 0
    
if(connected):
    print('Opening serial port')
    ard = serial.Serial(COM, 9600)
        
    while True:
        reply = s.recv(10)
        if(len(reply) < 1):
            print("Socket connection failed")
            break
        else:
            if reply[0] == 113: # 113 = 'q'
                print('Quiting')
                ard.close()
                s.close()
                break
            if len(reply) > 0:
                #print(reply) #Debugging
                ard.write(reply)

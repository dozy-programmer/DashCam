from multiprocessing import Process
from cv2 import cv2
import numpy as np
import time
import threading
import os
from datetime import datetime

import board
import adafruit_mlx90614
import socket
from subprocess import call


class send_data_thread(threading.Thread):
    def __init__(self, clientsocket, listensocket):
        threading.Thread.__init__(self)
        self.clientsocket = clientsocket
        self.listensocket = listensocket
    def run(self):
        send_data(self.clientsocket, self.listensocket)


class read_data_thread(threading.Thread):
    def __init__(self, clientsocket, listensocket):
        threading.Thread.__init__(self)
        self.clientsocket = clientsocket
        self.listensocket = listensocket

    def run(self):
        receive_data(self.clientsocket, self.listensocket)   


def convert_c_to_f(temp_in_c):
    return str(round(temp_in_c * (9/5) + 32, 2))


def re_connect_devices(listensocket):
    while True:
        print("Attempting to re-connect...")
        (clientsocket, address) = listensocket.accept()
        print("Device Re-connected")
        break
        
    return clientsocket


def connect_devices():
    Port = 8001
    maxConnections = 1000
    num_of_connections = 0
    IP = socket.gethostname()
    listensocket = socket.socket()
    listensocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    listensocket.bind(("", Port))
    listensocket.listen(maxConnections)
    print(f"Server {IP} on port {Port}")
    
    while True:
        print("Attempting to look for connection...")
        (clientsocket, address) = listensocket.accept()
        print("Device Connected")
        
        device_communication(clientsocket, listensocket)
        
        break


def device_communication(clientsocket, listensocket):
    thread4 = send_data_thread(clientsocket, listensocket)
    thread5 = read_data_thread(clientsocket, listensocket)
    thread4.start()
    thread5.start()
        
        
def send_data(clientsocket, listensocket):
    i2c = board.I2C()
    mlx = adafruit_mlx90614.MLX90614(i2c)
    
    print("Sending Heat sensor")
    while True:
        try:
            temp_to_f = convert_c_to_f(mlx.object_temperature) + "\n"
            clientsocket.send(temp_to_f.encode())
            print(f"Temp: {temp_to_f}")
        except:
            print("Connection was broken...trying again")
            clientsocket = re_connect_devices(listensocket)
            thread5 = read_data_thread(clientsocket, listensocket)
            thread5.start()
        time.sleep(2)
        
        
def receive_data(clientsocket, listensocket):
    print("Listening...")
    try:
        message = clientsocket.recv(1024)
        if message != '':
            command = message.decode('utf-8')
            print("Message received: " + message.decode('utf-8'))
            if command == "turn_off":
                clientsocket.close()
                print("Ras pi turned off - Testing instead of actually turning off")
                #call("sudo poweroff", shell=True)
            elif command == "stop_recording":
                print("Recording stopped")
                recording.release()
                out.release()
                cv2.destroyAllWindows()
    except:
        print("(Receiving) Connection was broken...trying again")
            
        time.sleep(5)
        
def change_res(recording, width, height):
        recording.set(3, width)
        recording.set(4, height)
        
        
def getDims(recording, res='1080p'):
        global STD_DIMENSIONS
        width, height = STD_DIMENSIONS['480p']
        if res in STD_DIMENSIONS:
            width, height = STD_DIMENSIONS[res]
        change_res(recording, width, height)
        return width, height
    
    
def getVidType(filename):
        global VIDEO_TYPE
        filename, ext = os.path.splitext(filename)
        if ext in VIDEO_TYPE:
            return VIDEO_TYPE[ext]
        return VIDEO_TYPE['mp4']
        
        
def start_recording():
    #start recording
    #stop recording and save
    #if new date, make a directory for that day
    #if starting new recording on same day, save video to same file

    now = datetime.now()
    date_time = now.strftime("%m_%d_%Y_%H_%M_%S")
    filename = date_time + ".mp4"
    frames = 40.0 #set the frame rate
    resolut = '1080p' #set the resolution

    #Dictionary of possible video dimensions
    global STD_DIMENSIONS
    STD_DIMENSIONS = {
        "480p": (640, 480),
        "720p": (1280, 720),
        "1080p:": (1920, 1080),
        "4k": (3840, 2160),
    }

    global VIDEO_TYPE
    #Codecc, mp4 will throw a warning, but still works
    VIDEO_TYPE = {
        '.avi': cv2.VideoWriter_fourcc(*'XVID'),
        '.mp4': cv2.VideoWriter_fourcc(*'H264'),
    }

    global recording, out
    recording = cv2.VideoCapture(0) #value '0' means camera is recording from default computer camera
    dims = getDims(recording, res=resolut)
    vidType = getVidType(filename)

    out = cv2.VideoWriter(filename, vidType, frames, dims) #(width, height)
    #Opens the window that shows the recording
    print("Recording started")
    while(True):
        ret, frame = recording.read()
        # uncomment line under this to unflip
        frame = cv2.flip(frame, 0)
        out.write(frame)
        #Displays the frame
        try:
            cv2.imshow('Recording ' + date_time, frame)
        except:
            break
        
         # stop the recording manually via "q" button
        if cv2.waitKey(20) & 0xFF == ord('q'): 
            # stops recording
            print("Recording stopped")
            recording.release()
            out.release()
            cv2.destroyAllWindows()
            break
    
        
if __name__ == '__main__':
    thread1 = threading.Thread(target=start_recording)
    thread2 = threading.Thread(target=connect_devices)
    
    thread1.start()
    thread2.start()
    
    print("Active threads", threading.activeCount())
    
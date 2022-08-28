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
from picamera import PiCamera

import recognition
import helper

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
        print("Device Reconnected")
        send_command = ""
        break_status = False
        device_communication(clientsocket, listensocket)
        break
        
    return clientsocket


def connect_devices():
    global host_name
    Port = 8001
    maxConnections = 1
    host_name = socket.gethostname()
    listensocket = socket.socket()
    listensocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    listensocket.bind(("", Port))
    listensocket.listen(maxConnections)
    print("Listening for connection...")
    
    while True:
        (clientsocket, address) = listensocket.accept()
        device_communication(clientsocket, listensocket)
        break


def device_communication(clientsocket, listensocket):
    thread4 = send_data_thread(clientsocket, listensocket)
    thread5 = read_data_thread(clientsocket, listensocket)
    thread4.start()
    thread5.start()
        
        
def send_data(clientsocket, listensocket):
    global send_command, host_name
    i2c = board.I2C()
    mlx = adafruit_mlx90614.MLX90614(i2c)
    
    while True:
        try:
            temp_to_f = helper.convert_c_to_f(mlx.object_temperature) + "_"+ send_command + "\n"
            clientsocket.send(temp_to_f.encode())
        except:
            clientsocket = re_connect_devices(listensocket)
            print("Connection was broken, reconnecting...")
            break
        time.sleep(2)
        
        
def receive_data(clientsocket, listensocket):
    global send_command
    break_status = False
    
    while break_status == False:
        print("Connected...\n")
        message = clientsocket.recv(1024)
        data = message.decode('utf-8')
        if data == "initiate_sending_image":
            break_status = True
            receive_image(clientsocket, listensocket)
            break
        elif data == "get_host_name":
            global host_name
            send_command = host_name + "*"
        elif data == "":
            re_connect_devices(listensocket)
            break
        else:
            time.sleep(2)
            
            
def receive_image(clientsocket, listensocket):
    global send_command
    send_command = "send_image"
    
    # ensures that 'Owner' folder exists and creates a new file name
    # based on number of images that already exist in folder
    owner_folder = helper.create_folder(os.getcwd(), "Owner")
    count = sum([len(files) for r, d, files in os.walk(owner_folder)])
    fname = owner_folder + "/owner" + str(count) + ".jpg"
    # opens file so that it can be written to
    f = open(fname, 'wb')
    
    # receives Image
    while True:
        data_received = clientsocket.recv(16384) # Gets incomming data
        if "image_sent".encode('utf_8') in data_received:
            received_image_size = os.path.getsize(fname) / 1024
            original_file_size = int(data_received.decode('utf-8').split('~')[1])
            if abs(received_image_size - original_file_size) <= 50:
                if recognition.detect_face(fname):
                    send_command = "image_received_success"
                else:
                    send_command = "image_received_fail"
            else:
                print(f"Received 'corrupted' File, resend image")
                send_command = "resend_image"
            break
        else:
            f.write(data_received) # writes data to file
    
    # restart listening to socket data
    thread5 = read_data_thread(clientsocket, listensocket)
    thread5.start()

        
def start_recording():
    global camera, recording_current_date_folder, recording_path
    camera = PiCamera()
    # check if recording folder exists and if not, create one
    recording_folder = helper.create_folder(os.getcwd(), "Recordings")
    
    # create file name based on date and current time
    now = datetime.now()
    current_date = now.strftime("%m_%d_%Y")
    recording_current_date_folder = helper.create_folder(recording_folder, current_date)
    current_time = now.strftime("%I_%M_%S_%p")
    file_name = current_time + ".h264"
    recording_path = recording_current_date_folder + "/" + file_name
    
    # preferences
    camera.vflip = False
    camera.framerate = 15
    camera.resolution = (720, 440)
    # start recording
    camera.start_recording(recording_path)
    print("Recording started...")
    camera.start_preview(fullscreen = False, window = (50,700,400,400))
    
    if os.path.exists(os.path.join(os.getcwd(), "Owner")) == True:
        owner_folder = os.path.join(os.getcwd(), "Owner")
        count = sum([len(files) for r, d, files in os.walk(owner_folder)])
        if count > 0 :
            start_detecting_driver()
        else:
            print("No owner photos found, please use app to set-up")
    else:
        print("No owner photos found, please use app to set-up")
            
    camera.wait_recording(1000)
    
    
def init():
    global break_status, send_command, host_name, camera, recording_path
    send_command = ""
    break_status = False
    
def detect_low_storage_space():
    
    gb_to_byte = 1000000000
    mb_to_byte = gb_to_byte / 1000
    max_folder_size = 45 * gb_to_byte # 40 GB converted to Bytes
    recording_dir = os.getcwd() + "/Recordings"
    
    while True:
        total_size = 0
        size_to_delete = 0
        files_to_delete = []
        # walks through all sub directories and files to get dir size
        for path, dirs, files in os.walk(recording_dir):
            files.sort()
            for f in files:
                fp = os.path.join(path, f)
                current_size = os.path.getsize(fp)
                total_size += current_size
                
                # gets oldest files that add up to be at least 1 GB in size 
                if total_size <= gb_to_byte:
                    files_to_delete.append(path + "/" + f)
                    size_to_delete = size_to_delete + current_size
        
        # if size of directory exceeds 45 GB (51 max size on SD card), then at least
        # 1 GB of files are deleted to make space 
        if total_size >= max_folder_size:
            for file in files_to_delete:
                os.remove(file)
            print(f"Size of Recording Directory exceeds 45 GB: {int(size_to_delete/mb_to_byte)} MB")
            print(f"Deleted {int(size_to_delete/mb_to_byte)} MB of files")
            print(f"New Recording Directory Size: {int((total_size-size_to_delete)/mb_to_byte)} MB")
        
        # check every hour
        time.sleep(60 * 60)
        
def start_detecting_driver():
    global camera, recording_path
    thread4 = threading.Thread(target=recognition.get_driver_photo)
    thread4.start()
        
if __name__ == '__main__':
    init()
    
    thread1 = threading.Thread(target=start_recording)
    thread2 = threading.Thread(target=connect_devices)
    thread3 = threading.Thread(target=detect_low_storage_space)
    
    thread1.start()
    thread2.start()
    thread3.start()
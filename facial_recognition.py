import os

from cv2 import cv2
from os.path import exists
import threading
from deepface import DeepFace


# Simply checks in the folder in stated path if owner's verification image exists
def facial_recognition_check():
    if exists("owner.jpeg"):
        return True
    else:
        return False


# Simple set-up process if it is a first time user
# Once the camera window shows up, user have to manually press c with their face facing forward
def facial_recognition_setup(cam_name, cam_num):
    cv2.namedWindow(cam_name)
    camera = cv2.VideoCapture(cam_num, cv2.CAP_DSHOW)
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

    if camera is None or not camera.isOpened():
        cv2.destroyWindow(cam_name)
        print(f"{cam_name} is not functioning")
        return

    while True:
        ret, image = camera.read()
        image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(image_gray, 1.1, 4)

        for (x, y, w, h) in faces:
            cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 255), 2)
        cv2.imshow(cam_name, image)

        if cv2.waitKey(2) == ord('c'):
            cv2.imwrite("owner.jpeg", img=image)
            camera.release()
            cv2.destroyWindow(cam_name)
            break


# Current user have to manually take picture, and it will verify if it matches with the owner photo
# After that it will remove the picture
def facial_recognition_verification(cam_name, cam_num):
    cv2.namedWindow(cam_name)
    camera = cv2.VideoCapture(cam_num, cv2.CAP_DSHOW)
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

    if camera is None or not camera.isOpened():
        cv2.destroyWindow(cam_name)
        print(f"{cam_name} is not functioning")
        return

    # counter
    isCaptured = False

    while not isCaptured:
        ret, image = camera.read()
        image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(image_gray, 1.1, 4)

        for (x, y, w, h) in faces:
            cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 255), 2)
        cv2.imshow(cam_name, image)

        if cv2.waitKey(2) == ord('c'):
            cv2.imwrite("verify.jpeg", img=image)
            camera.release()
            cv2.destroyWindow(cam_name)
            isCaptured = True

    models = ["VGG-Face", "Facenet", "Facenet512", "OpenFace", "DeepFace", "DeepID", "ArcFace", "Dlib"]
    face_verification = DeepFace.verify("owner.jpeg", "verify.jpeg", models[0])
    os.remove("verify.jpeg")
    return face_verification


class ScanThread(threading.Thread):
    def __init__(self, cam_name, cam_num):
        threading.Thread.__init__(self)
        self.cam_name = cam_name
        self.cam_num = cam_num

    def run(self):
        facial_recognition_setup(self.cam_name, self.cam_num)

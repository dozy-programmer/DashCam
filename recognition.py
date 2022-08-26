from cv2 import cv2
import os
import face_recognition
import helper
import time


def detect_face(image_to_check):
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
    img = cv2.imread(image_to_check)
    faces = face_cascade.detectMultiScale(img, 1.1, 4)
    is_face_detected = False

    for (x, y, w, h) in faces:
        cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 255), 2)
        if x or y or w or h > 0:
            is_face_detected = True
     
    # delete image received if it does not contain a face 
    if is_face_detected == False:
        os.remove(image_to_check)
        print(f"Face not detected in photo, deleting received file {image_to_check}")
    else:
        print("Face detected in received Photo")
        # catches error if file exists and deletes it
        try:        
            cv2.imwrite("face_detected.png", img)
        except:
            os.remove(face_detected.png)
            cv2.imwrite("face_detected.png", img)
    
    return is_face_detected;


def get_driver_photo():
    # takes a picture when a face is detected and compares it to owner photos
    print("Looking for Owner...")
    driver_folder = helper.create_folder(os.getcwd(), "Unverified_Driver")
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
    webcam = cv2.VideoCapture(1)
    webcam.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    looking_for_driver = True
    save_verification_pic = ""
    start = time.time()
        
    while looking_for_driver:
        ret, image = webcam.read()
        image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(image_gray, 1.1, 4)

        for (x, y, w, h) in faces:
            if x > 0 or y > 0 or w > 0 or h > 0:
                print("Face detected, comparing with owner photos")
                count = sum([len(files) for r, d, files in os.walk(driver_folder)])
                current_driver_photo = driver_folder + "/driver_" + str(count) + ".jpg"
                cv2.imwrite(current_driver_photo, image)
                if compare_faces(current_driver_photo):
                    # owner detected, stop looking
                    looking_for_driver = False
                break
            else:
                print("Face not detected")
                
        end = time.time()
        # attempts to find owner for 5 mins
        if end-start >= 300:
            print(f"Owner not found")
            looking_for_driver = False
                
    webcam.release()
    cv2.destroyAllWindows()


def compare_faces(current_driver):
    owner_dir = os.getcwd() + '/' + "Owner"
    owner_pics = os.listdir(owner_dir)
    
    for pic in owner_pics:
        try:
            picture_of_owner = face_recognition.load_image_file(owner_dir + '/' + pic)
            owner_encoding = face_recognition.face_encodings(picture_of_owner)[0]
            unknown_driver = face_recognition.load_image_file(current_driver)
            unknown_driver_encoding = face_recognition.face_encodings(unknown_driver)[0]
        except:
            print("Error comparing faces")
            return False
            
        results = face_recognition.compare_faces([owner_encoding], unknown_driver_encoding)

        if results[0] == True:
            print("Owner verified!")
            os.remove(current_driver)
            return True
        else:
            print("Detected another face!")
            time.sleep(10)
            return False

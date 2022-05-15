import os
import dashcam


def convert_c_to_f(temp_in_c):
    return str(round(temp_in_c * (9/5) + 32, 2))


def create_folder(current_dir, folder_name):
    folder_path = os.path.join(current_dir, folder_name)
    if os.path.exists(folder_path) == False:
        file = os.path.join(folder_path, folder_name)
        os.makedirs(folder_path)
    return folder_path
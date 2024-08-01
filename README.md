# monument-recognition
The Monument Recognition Application is a mobile app driven by machine learning and object detection technology. It enables real-time identification of historical monuments using your phone's camera. Additionally, it provides in-depth information about the recognized monuments. 

The mobile app employs a MobileNetSSD V2 object detection model, which has been trained on a custom dataset, to initially identify monuments in the camera feed. Once detected, the app does an API call to the server hosted on virtual machine in Microsoft Azure where the inferencing model named YOLO V7 is deployed which does the identification of the monument. Monument information and location coordinates are stored in a PostgreSQL database. An intelligent feature validates the monument's identification by comparing real-time device coordinates with monument location coordinates. Moreover, the app offers recommendations for nearby amenities such as hotels and ATMs based on location coordinates. The machine learning models were trained on a custom dataset prepared by us.  

For the further information on the project like dataset, model performance etc., please refer the documentation provided in the code section.

## Installation
A. Inferencing Model Driver and API Source Code:
 - Can be opened and edited in the code editor like VS code.
     
B. Android Application Source Code:
- Can be opened in the Android Studio.






## Usage

The apk file is available at the Releases section. Download and install it.

## Youtube Link Showing the Demo
[Monument Recognition App's Demo](https://www.youtube.com/embed/JQKLWwSyBII)  

## Note  

Currently, the YOLO V7 model deployed on remote server is offline so, the app cannot do the monument identification. However, monument detection by the model running on the phone works as intended.

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

# E-Voting

Voting Mobile application with implementation of Optical Character Recognition and Face Detection Using MTCNN (Multi Task Convolutional Neural Network) and face recognition with FaceNet Model

# ERD 
![erdiagram](https://user-images.githubusercontent.com/64388690/162124185-136746fd-0b64-4270-bf1c-ac0cb823d995.png)


User could choose one candidate only as seen above. The user Identitiy number will be then stored in Firebase.

# Flowchart
![Register Flowchart](https://user-images.githubusercontent.com/64388690/162125006-f314810f-146c-4467-9f3c-463f612b22dd.png)
User has to register their face and identitiy card before login in.
The ML- Kit Firebase will detect the personal ID and passed it to the next page of registration. User need to take a selfie picture in the next process.
![votingflow](https://user-images.githubusercontent.com/64388690/162125309-344b4c54-8491-45ed-b1f4-93a3b9a5b12b.png)
User then will be taken to the face matching section before the election activity. 

# Optical Character Recognition with ML Kit Firebase

Firebase has an embedded text detection library which is very easy to use. First we need to implement the library to the build.gradle and then make the firebase vision image object. Lastly, we call the detector. Follow this link for details https://developers.google.com/ml-kit/vision/text-recognition/android . The result as shown below.

<img width="540" alt="promptnik" src="https://user-images.githubusercontent.com/64388690/162125411-809e794e-aeac-42cd-820b-ec31754973ff.png">

After calling the method ondevicetextrecognizer which has been provided by the ml - kit, then the regular expression will filter the desire output as below.

![ocr](https://user-images.githubusercontent.com/64388690/162125592-d6b05e56-06cb-4cdf-9014-a47f9209fd6e.JPG)

# Face Detection using MTCNN

Multi task Convolutional Network has three layer stage to detect faces in a picture, which is Proposal net, Refine Net and Output Net. Each layer provided input for the next layer and so on. The output consists of bounding box regression, confidence level and facial landmarks.


![detectfaces](https://user-images.githubusercontent.com/64388690/162126298-145a4187-1f95-4ef1-8be0-3321bbc0068a.JPG)


# Face Recognition using FaceNet model and tensorflowlite Interpreter
To access our trained model we need tensorflowlite which allow us to do so on Java programming on Android. We will load our model( .pb) and the converted ones (.tflite) in the folder assets. Then we will call the function as shown below.
![runFaceNetModel](https://user-images.githubusercontent.com/64388690/162126929-4bc1cdca-562e-440a-9e28-70b9766ef960.JPG)

Using Eucledian, we would calculate the vector embeddings, resulting in a float distance that represents the similarity between 2 faces. If it is closer to 0 then it is considered identical.

# Result



https://user-images.githubusercontent.com/64388690/162130955-5aff9bed-a37c-44d9-ad79-43bffe301d13.mp4



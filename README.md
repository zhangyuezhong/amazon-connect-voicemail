**amazon Connect voicemail to email**

Amazon Connect doesn't provide voicemail feature, this project is to implement an voicemail to email

The contact flow provide option for caller to leave a voice message if the time is outside business hours,  the voice message will be sent to an email address specified. 

The contact flow use the block "start media stream" and stop media stream" to stream the caller's voice to Kinesis Video stream.   it use the "Get User Input" block to wait for caller to press 1 to stop the media stream. 

the contact flow invokes a lambda function to 
- read the audio from Kinesis video stream and save into a wav file in /tmp.
- send email to the recipient with audio file as attachment. (Amazon SES),  please ensure the email address is verified.


To deploy the project (lambda function)
- update the serverless.yaml  as requried 
- lambda must the role permission (read kinesis video stream, send email)

1. - mvn clean package
2. - serverless deploy 


![Test Image 6](https://raw.githubusercontent.com/zhangyuezhong/amazon-connect-voicemail/master/screenshots/add%20lambda.png)


# amazon-connect-voicemail
amazon Connect voicemail to email


Amazon Connect doesn't provide voicemail feature, this project is to implement an voicemail to email

the project include  a contact flow to play a prompt ask caller to leave voice message, the contact flow start stream the audio to kinesis video stream, and stop the stream of audio once caller press 1 or #. 

a lambda function is invoke upon stop the stream, the lambda function read the stream data into a wav.file and send it to an email address.

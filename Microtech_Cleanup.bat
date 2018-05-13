TITLE MICROTECH CLEANUP

REM Log File Name
set ACTIVITY_LOG="activities_cleanup"


REM Microtech ecnrypted content folder
set MICROTECH_ENC_CONT_DIR=C:\Microtech_Encryption\Encrypted_Content

REM Microtech jobs dir
set ENCRYPTION_JOBS_DIR=C:\Jobs

REM Microtech jobs archive dir
set ENCRYPTION_JOBS_ARCHIVE_DIR=C:\Jobs\Archive

REM job status file xtention
set STATUS_FILE_XTN=.res

REm content type wildcard
set CONTENT_TYPE=*.iso

REM log4j props location
set LOG4J_LOC=C:/Microtech_Encryption/cleanup-log4j.properties

c:\java8\bin\java -Dactivity_log=%ACTIVITY_LOG% -cp .;C:/Microtech_Encryption/Microtech_listener.jar; com.encrypt.Cleanup %MICROTECH_ENC_CONT_DIR% %ENCRYPTION_JOBS_DIR% %ENCRYPTION_JOBS_ARCHIVE_DIR% %STATUS_FILE_XTN% %CONTENT_TYPE% %LOG4J_LOC%
pause
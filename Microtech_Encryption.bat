TITLE MICROTECH ENCRYPTION LISTENER

REM Log File Name
set ACTIVITY_LOG="activities_encrypt"

REM The listener watches this directory PAT batch files
set WATCH_DIR_PAT_JOBS=C:\Jobs\pat-e

REM Processed batches are moved to this folder
set PROCESSED_JOBS=C:\Microtech_Encryption\Encrypted_Jobs

REM POLL INTERVAL 
set POLL_INTERVAL=60

REM Encrypted files are moved to this folder
set ENCRYPTION_OUTPUT=C:\Microtech_Encryption\Encrypted_Content

REM Microtech Job Folder
set MICROTECH_JOB_DIR=C:\Jobs

REM Encryption Process
set ENCRYPTION_PROCESS=C:\Microtech_Encryption\Encrypt.bat

REM Encryption Log
set ENCRYPTION_LOG=C:\Microtech_Encryption\Logs\encryption_status.log

REM Job file extension
set JOB_FILE_EXTENSION=job

REM Thread pool size
set THREAD_POOL_SIZE=7

REM log4j props location
set LOG4J_LOC=C:/Microtech_Encryption/encrypt-log4j.properties

c:\java8\bin\java -Dactivity_log=%ACTIVITY_LOG% -cp .;C:/Microtech_Encryption/Microtech_listener.jar; com.encrypt.Driver %WATCH_DIR_PAT_JOBS% %PROCESSED_JOBS% %POLL_INTERVAL% %ENCRYPTION_OUTPUT% %MICROTECH_JOB_DIR% %ENCRYPTION_PROCESS% %ENCRYPTION_LOG% %JOB_FILE_EXTENSION% %THREAD_POOL_SIZE% %LOG4J_LOC%
pause
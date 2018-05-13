call D:\Microtech_Encryption\copycontent.bat %~1 %~2 
call D:\dvdxcaller\Win\dvdx_caller.exe %~2 %~3
set error=%ERRORLEVEL%
set source=%~1
IF %error% NEQ 0 ( set Timestamp=%date:~4,2%%date:~7,2%%date:~10,4%_%time:~0,2%%time:~3,2%%time:~6,2%
call D:\Microtech_Encryption\mailme_failure.bat %~1 "return_code=%error%"
echo Encryption failed for file %~1 with return code=%error% >> "D:\Microtech_Encryption\Logs\encryption_error_%error%_%Timestamp%.log"
del /f %~2
exit /b %error% )
del /f %~2







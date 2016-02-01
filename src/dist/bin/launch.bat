set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set AUTOMETER_HOME=%DIRNAME%..

%DIRNAME%autometer.bat -d %DIRNAME%.. $*

@echo off
cd /d "%~dp0"
call mvnw.cmd clean test -Papi-tests
pause

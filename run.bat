@echo off
REM Chay tool VXMM voi backend self-host (localhost:8009)
REM Yeu cau truoc khi chay:
REM   - docker start vxmm-pg
REM   - cd backend && npm run dev (port 8009)

cd /d "%~dp0"
java -cp "libs\*" main.Application --local
pause

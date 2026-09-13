@echo off
set NODE_PATH=D:\Program Files\nodejs
set PATH=%NODE_PATH%;%PATH%
cd /d %~dp0
echo Starting Vue development server...
node -v
npm run dev
pause

# iot-platform
## 基础框架
RuoYi v3.9.2
演示地址：http://vue.ruoyi.vip
文档地址：https://doc.ruoyi.vip/ruoyi-vue/
## 开发环境安装
### 安装jdk
网盘下载jdk-17.0.14_windows-x64_bin.msi，默认安装  
### 安装node
网盘下载node-v24.14.0-x64.msi，默认安装
### 安装mysql
1.解压  
网盘下载mysql-5.7.44-winx64.zip，解压到无中文、无空格的路径（如 D:\mysql）
![img.png](images/img.png)
2.my.ini  
下载my.ini,放在D:\mysql中
![img_1.png](images/img_1.png)
3.初始化与安装  
**注意，如果已经安装，需先删除data目录（D:\mysql\data），这操作会清空之前的所有数据**  
cd D:\mysql\bin  
.\mysqld --initialize --console
![img_2.png](images/img_2.png)
**注意，最后一行有临时密码**  

.\mysqld --install  
net start mysql  
4.登录并修改密码  
.\mysql -u root -p  
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';  
FLUSH PRIVILEGES;  
quit;  
4.如果忘记登录密码  
（1）停止服务：net stop mysql  
（2）用 skip-grant-tables 启动 MySQL  
cd C:\mysql\bin  
.\mysqld --defaults-file="D:\mysql\my.ini" --skip-grant-tables  
这时程序会卡住，此窗口不要关闭，再开一个新CMD窗口  
cd D:\mysql\bin  
.\mysql -u root -p  
不用密码直接回车  
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';  
FLUSH PRIVILEGES;  
quit;  
5.删除MySQL
**如果需要卸载MYSQL，可以使用以下指令**  
net stop mysql  
.\mysqld --remove  
重启电脑
### 安装redis
1.解压  
网盘下载Redis-x64-5.0.14.1.zip，解压到无中文、无空格的路径（如 D:\Redis） 
2.安装  
cd D:\Redis    
.\redis-server --service-install redis.windows.conf --loglevel verbose  
.\redis-server --service-start  
3.卸载  
.\redis-server --service-stop  
.\redis-server --service-uninstall
### 安装Navicat
网盘下载Navicat Premium 17.0.8 (x64).rar，解压后默认安装

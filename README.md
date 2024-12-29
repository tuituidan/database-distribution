# 数据分发服务

<img src="https://img.shields.io/badge/springboot-2.7.4-brightgreen" alt="springboot"/>   <img src="https://img.shields.io/badge/jdk-1.8-blue" alt="java"/>   <img src="https://img.shields.io/badge/vue-2.6.12-blueviolet" alt="vue"/>   <img src="https://img.shields.io/badge/elementui-2.15.14-brown" alt="element-ui"/>   <img src="https://img.shields.io/badge/mysql8-yellow" alt="java"/> 

## 简介

基于mysql的数据分发服务，服务监听mysql的binlog，将数据通过http发送给其他服务，轻量级，无其他依赖，独立jar部署。

**Github:** https://github.com/tuituidan/database-distribution

**Gitee:** https://gitee.com/tuituidan/database-distribution


## 功能说明


## 在线体验

演示地址：

文档地址：

## 技术栈

#### 前端主要技术

vue2+ElementUI，前后端分离开发，合并部署。

#### 后端主要技术

Springboot、Mybatis、SpringSecurity

## 部署说明

通过常规maven命令构建，打包执行命令`mvn clean package -Dmaven.npm.skip=false`，会自动将前端构建到后端的jar包中，这样整个服务就一个jar包（当然如果想前后端分离部署也是可以的，这里不再赘述），再无其他依赖服务，仅需要jdk环境即可启动。

## license

100%开源，MIT协议，可自由修改

## 页面展示

首页

![首页](https://github.com/user-attachments/assets/b0ea7432-92f1-477c-8dec-fd1ea5f30b3e)

编辑数据源

![编辑数据源](https://github.com/user-attachments/assets/e72a27eb-8692-4e61-8be7-37def32bf882)

数据源配置：

![数据源配置](https://github.com/user-attachments/assets/c6865842-03ae-4412-9396-c388399fcef9)

增量手动推送

![增量手动推送](https://github.com/user-attachments/assets/b733b893-942b-40eb-82e2-23095051462a)

编辑应用

![编辑应用](https://github.com/user-attachments/assets/a629c7a5-1588-4d5c-a2db-abefdb080055)

应用配置

![应用配置](https://github.com/user-attachments/assets/af371661-09f2-451b-877f-42c062a0388f)

预警配置

![预警配置](https://github.com/user-attachments/assets/17bdd009-db59-4e25-bc04-811204484c32)

数据日志

![数据日志](https://github.com/user-attachments/assets/38185b95-289f-45ec-a39d-e7cc3c1d5ba5)

推送日志

![推送日志](https://github.com/user-attachments/assets/a5b6bff4-6bfe-4582-8b8d-4f0af70ee863)

手动数据重推

![手动数据重推](https://github.com/user-attachments/assets/40e2fa18-170a-4c24-84a6-bd6a959466c0)

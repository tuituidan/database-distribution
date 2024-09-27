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


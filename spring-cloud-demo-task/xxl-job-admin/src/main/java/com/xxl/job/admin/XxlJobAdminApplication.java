package com.xxl.job.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
  * @author zhanglong
  * @className XxlJobAdminApplication
  * @methodName
  * @description: 启动类
  * @param
  * @return
  * @date 2019-08-03 14:10
  *
  */
@EnableDiscoveryClient
@SpringBootApplication
public class XxlJobAdminApplication {

	public static void main(String[] args) {
        SpringApplication.run(XxlJobAdminApplication.class, args);
	}

}
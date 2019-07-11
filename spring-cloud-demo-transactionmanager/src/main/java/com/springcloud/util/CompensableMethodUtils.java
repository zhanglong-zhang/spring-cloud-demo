package com.springcloud.util;

import com.springcloud.context.SpringTransactionContext;
import com.springcloud.enums.MethodType;

/**
 * @title: CompensableMethodUtils
 * @projectName spring-cloud-demo
 * @description: TODO
 * @author zhanglong
 * @date 2019-07-1020:17
 */

public class CompensableMethodUtils {
  /**
   * 计算方法类型.
   * @param transactionContext
   * @param isCompensable
   * @return
   */
  public static MethodType calculateMethodType( SpringTransactionContext transactionContext, boolean isCompensable) {

    if (transactionContext == null && isCompensable) {
      // 没有事务上下文信息，并且方法有事务注解的，为可补偿事务根方法（也就是事务发起者）
      //isRootTransactionMethod
      return MethodType.ROOT;
    } else if (transactionContext == null && !isCompensable) {
      // 没有事务上下文信息，并且方法没有事务注解的，为可补偿事务服务消费者（参考者）方法（一般为被调用的服务接口）
      //isSoaConsumer
      return MethodType.CONSUMER;
    } else if (transactionContext != null && isCompensable) {
      // 有事务上下文信息，并且方法有事务注解的，为可补偿事务服务提供者方法（一般为被调用的服务接口的实现方法）
      //isSoaProvider
      return MethodType.PROVIDER;
    } else {
      return MethodType.NORMAL;
    }
  }

  /**
   * 获取事务上下文参数的位置.
   * @param parameterTypes
   * @return
   */
  public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

    int i = -1;

    for (i = 0; i < parameterTypes.length; i++) {
      if (parameterTypes[i].equals(SpringTransactionContext.class)) {
        break;
      }
    }
    return i;
  }
  /**
   * 从参数获取事务上下文.
   * @param args
   * @return
   */
  public static SpringTransactionContext getTransactionContextFromArgs(Object[] args) {

    SpringTransactionContext transactionContext = null;

    for (Object arg : args) {
      if (arg != null && SpringTransactionContext.class.isAssignableFrom(arg.getClass())) {

        transactionContext = (SpringTransactionContext) arg;
      }
    }

    return transactionContext;
  }
}

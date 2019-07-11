package com.springcloud.manager;

import com.springcloud.configurator.TransactionConfigurator;
import com.springcloud.context.SpringTransactionContext;
import com.springcloud.context.TransactionXid;
import com.springcloud.enums.TransactionStatus;
import com.springcloud.enums.TransactionType;
import com.springcloud.exception.CancellingException;
import com.springcloud.exception.ConfirmingException;
import com.springcloud.exception.NoExistedTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @title: TransactionManager
 * @projectName spring-cloud-demo
 * @description: TODO
 * @author zhanglong
 * @date 2019-07-1122:40
 */
@Component
public class TransactionManager {
  static final Logger LOG = LoggerFactory.getLogger(TransactionManager.class.getSimpleName());

  /**
   * 事务配置器
   */
  @Autowired
  private TransactionConfigurator transactionConfigurator;

  /**
   * 定义当前线程的事务局部变量.
   */
  private ThreadLocal<Transaction> threadLocalTransaction = new ThreadLocal<Transaction>();

  public TransactionManager(TransactionConfigurator transactionConfigurator) {
    this.transactionConfigurator = transactionConfigurator;
  }



  /**
   * 事务开始（创建事务日志记录，并将该事务日志记录存入当前线程的事务局部变量中）
   */
  public void begin() {
    LOG.debug("==>begin()");
    Transaction transaction = new Transaction(TransactionType.ROOT); // 事务类型为ROOT:1
    LOG.debug("==>TransactionType:" + transaction.getTransactionType().toString() + ", Transaction Status:" + transaction.getStatus().toString());
    TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
    transactionRepository.create(transaction); // 创建事务记录,写入事务日志库
    threadLocalTransaction.set(transaction); // 将该事务日志记录存入当前线程的事务局部变量中
  }

  /**
   * 基于全局事务ID扩展创建新的分支事务，并存于当前线程的事务局部变量中.
   * @param transactionContext
   */
  public void propagationNewBegin(SpringTransactionContext transactionContext) {

    Transaction transaction = new Transaction(transactionContext); // 事务ID不变
    LOG.debug("==>propagationNewBegin TransactionXid：" + TransactionXid
        .byteArrayToUUID(transaction.getXid().getGlobalTransactionId()).toString()
        + "|" + TransactionXid.byteArrayToUUID(transaction.getXid().getBranchQualifier()).toString());

    transactionConfigurator.getTransactionRepository().create(transaction);

    threadLocalTransaction.set(transaction); // 存于当前线程的事务局部变量中
  }
  /**
   * 提交.
   */
  public void commit() {
    LOG.debug("==>TransactionManager commit()");
    Transaction transaction = getCurrentTransaction();

    transaction.changeStatus(TransactionStatus.CONFIRMING);
    LOG.debug("==>update transaction status to CONFIRMING");
    transactionConfigurator.getTransactionRepository().update(transaction);

    try {
      LOG.info("==>transaction begin commit()");
      transaction.commit();
      transactionConfigurator.getTransactionRepository().delete(transaction);
    } catch (Throwable commitException) {
      LOG.error("compensable transaction confirm failed.", commitException);
      throw new ConfirmingException(commitException);
    }
  }
  /**
   * 获取当前事务.
   * @return
   */
  public Transaction getCurrentTransaction() {
    return threadLocalTransaction.get();
  }

  /**
   * 回滚事务.
   */
  public void rollback() {

    Transaction transaction = getCurrentTransaction();
    transaction.changeStatus(TransactionStatus.CANCELLING);

    transactionConfigurator.getTransactionRepository().update(transaction);

    try {
      LOG.info("==>transaction begin rollback()");
      transaction.rollback();
      transactionConfigurator.getTransactionRepository().delete(transaction);
    } catch (Throwable rollbackException) {
      LOG.error("compensable transaction rollback failed.", rollbackException);
      throw new CancellingException(rollbackException);
    }
  }


  /**
   * 找出存在的事务并处理.
   * @param transactionContext
   * @throws NoExistedTransactionException
   */
  public void propagationExistBegin(SpringTransactionContext transactionContext) throws NoExistedTransactionException {
    TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
    Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());

    if (transaction != null) {

      LOG.debug("==>propagationExistBegin TransactionXid：" + TransactionXid.byteArrayToUUID(transaction.getXid().getGlobalTransactionId()).toString()
          + "|" + TransactionXid.byteArrayToUUID(transaction.getXid().getBranchQualifier()).toString());

      transaction.changeStatus(TransactionStatus.valueOf(transactionContext.getStatus()));
      threadLocalTransaction.set(transaction);
    } else {
      throw new NoExistedTransactionException();
    }
  }
}

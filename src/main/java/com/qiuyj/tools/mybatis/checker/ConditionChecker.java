package com.qiuyj.tools.mybatis.checker;

import com.qiuyj.tools.mybatis.SqlInfo;

import java.lang.reflect.Field;

/**
 * 检查器
 * @author qiuyj
 * @since 2017/11/20
 */
public interface ConditionChecker {

  /**
   * 当前检查的Field跳出剩余所有的检查器
   */
  int BREAK_CURRENT = -1;

  /**
   * 当前检查的Field继续剩下的检查器
   */
  int CONTINUE_EXECUTION = 0;

  /**
   * 当前检查的Field跳过接下来的一个检查器
   */
  int SKIP_ONE = 1;

  /**
   * 当前检查的Field跳过接下来的两个检查器
   */
  int SKIP_TWO = 2;

  /**
   * 当前检查的Field跳过接下来的三个检查器
   */
  int SKIP_THREE = 3;

  /**
   * 当前检查的Field跳过接下来的四个检查器
   */
  int SKIP_FOUR = 4;

  /**
   * 检查对应的javabean的属性是否满足对应的关系
   * @param field 属性的Field对象
   * @param sqlInfo 当前bean的sqlInfo对象
   * @return {int} 如果返回-1，那么就不会继续执行剩下的检查器了，
   *    如果返回0，那么就继续执行剩下的检查器
   *    如果返回大于0的整数，那么表示跳过后面的几个检查器继续执行剩下的检查器（可能会发生异常，如果返回的n大于剩下的检查器的个数）
   */
  int doCheck(Field field, SqlInfo sqlInfo);

  /**
   * 将一个属性按照javabean规范转换成对应的getter方法名
   */
  default String fieldToGetterName(Field field) {
    char[] chs = field.getName().toCharArray();
    chs[0] = Character.toUpperCase(chs[0]);
    StringBuilder sb =
        (field.getType() == Boolean.TYPE || field.getType() == Boolean.class)
            ? new StringBuilder("is") : new StringBuilder("get");
    sb.append(chs);
    return sb.toString();
  }
}
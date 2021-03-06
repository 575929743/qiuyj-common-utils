package com.qiuyj.tools.mybatis.engine;

import com.qiuyj.tools.mybatis.MapperMethodResolver;
import com.qiuyj.tools.mybatis.config.SqlGeneratorConfig;
import com.qiuyj.tools.mybatis.mapper.Mapper;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Method;

/**
 * @author qiuyj
 * @since 2017/11/15
 */
public interface SqlGeneratorEngine {

  static SqlGeneratorEngine determineSqlGenerator(SqlGeneratorConfig config, MapperMethodResolver resolver) {
    switch (config.getDatabaseType()) {
      case ORACLE:
        return new OracleSqlGeneratorEngine(config.getCheckerChain(), config.getBaseSqlProvider(), resolver);
      case MYSQL:
      default:
        return new MySQLSqlGeneratorEngine(config.getCheckerChain(), config.getBaseSqlProvider(), resolver);
    }
  }

  /**
   * 生成对应的sql
   * @param ms mappedStatement
   * @param mapperClass Mapper接口的Class对象
   * @param method mapper方法
   * @param args 参数
   */
  void generateSql(MappedStatement ms,
                   Class<? extends Mapper<?, ?>> mapperClass,
                   Method method,
                   Object args);

  /**
   * 分析mapper
   */
  void analysis(Class<? extends Mapper<?, ?>> actualMapperClass);
}
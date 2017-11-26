package com.qiuyj.tools.mybatis.engine;

import com.qiuyj.tools.ReflectionUtils;
import com.qiuyj.tools.mybatis.SqlInfo;
import com.qiuyj.tools.mybatis.build.SqlProvider;
import com.qiuyj.tools.mybatis.checker.CheckerChain;
import com.qiuyj.tools.mybatis.mapper.Mapper;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiuyj
 * @since 2017/11/15
 */
public abstract class AbstractSqlGeneratorEngine implements SqlGeneratorEngine {
  private final Object sqlInfoLock = new Object();
  private final Map<Class<? extends Mapper>, SqlInfo> sqlInfos = new HashMap<>();
  private final CheckerChain chain;
  private final SqlProvider baseSqlProvider;

  protected AbstractSqlGeneratorEngine(CheckerChain chain, SqlProvider sqlProvider) {
    this.chain = chain;
    baseSqlProvider = sqlProvider;
  }

  @Override
  public void analysis(Class<? extends Mapper<?, ?>> actualMapperClass) {
    if (!sqlInfos.containsKey(actualMapperClass)) {
      synchronized (sqlInfoLock) {
        if (!sqlInfos.containsKey(actualMapperClass)) {
          SqlInfo mapperSqlInfo = new SqlInfo(actualMapperClass, chain);
          sqlInfos.put(actualMapperClass, mapperSqlInfo);
        }
      }
    }
  }

  /**
   * 该方法无需同步处理，因为当执行该方法的时候，执行顺序可以保证缓存中一定有SqlInfo
   */
  private SqlInfo getSqlInfo(Class<? extends Mapper<?, ?>> mapperClass) {
    return sqlInfos.get(mapperClass);
  }

  @Override
  public void generateSql(MappedStatement ms, Class<? extends Mapper<?, ?>> mapperClass, Method mapperMethod, Object args) {
    MetaObject msMetaObject = ms.getConfiguration().newMetaObject(ms);
    SqlInfo sqlInf = getSqlInfo(mapperClass);

    // 首先得到对应的SqlNode
    Method sqlNodeMethod = ReflectionUtils.getDeclaredMethod(baseSqlProvider.getClass(), mapperMethod.getName(), ms.getClass(), SqlInfo.class);
    SqlNode sqlNode = (SqlNode) ReflectionUtils.invokeMethod(baseSqlProvider, sqlNodeMethod, ms, getSqlInfo(mapperClass));
    SqlSource sqlSource;
    if (sqlNode instanceof MixedSqlNode)
      sqlSource = new DynamicSqlSource(ms.getConfiguration(), sqlNode);
    else {
      DynamicContext dc = new DynamicContext(ms.getConfiguration(), args);
      sqlNode.apply(dc);
      // 有两个参数以上或者一个参数，但这个参数是Collection类型或是数组类型
      // 如果是两个参数以上，那么args是ParamMap类型
      // 如果是一个参数并且是Collection类型或者数组，那么args是StrictMap类型
      if (args instanceof Map) {

      }
      sqlSource = new StaticSqlSource(ms.getConfiguration(), dc.getSql());
    }
    // 重新设置sqlSource，即可生成sql语句
    msMetaObject.setValue("sqlSource", sqlSource);
  }
}
package util;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Created by yangmingquan on 2018/9/7.
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {

}

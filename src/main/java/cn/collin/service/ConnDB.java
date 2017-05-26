package cn.collin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Collin on 2017/4/22.
 */
@Service
public class ConnDB {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insert(String serverAd, String chaincodeId, String invokeId, long invokeBegin, long invokeEnd){
        String sql = "insert into tran_data values(\'"+serverAd+"\',\'"+chaincodeId+"\',\'"+invokeId+"\',\'"+0+"\',\'"+invokeBegin+"\',\'"+invokeEnd+"\')";
//        System.out.println(sql);
        jdbcTemplate.execute(sql);
    }

    public List queryInitData(){
        String sql = "select invoke_id, invoke_begin from tran_data where status = 0 ";
//        System.out.println(sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list;
    }

    public void updateTime(String invokeId, String invokeTime){
        String sql = "update tran_data set invoke_end = \'" + invokeTime + "\', status = \'1\' where invoke_id = \'" +  invokeId +"\'";
//        System.out.println(sql);
//        System.out.println(sql);
        jdbcTemplate.execute(sql);
    }
}

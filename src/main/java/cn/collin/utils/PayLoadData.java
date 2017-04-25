package cn.collin.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Created by Collin on 2017/4/16.
 */
public class PayLoadData {
    public HttpEntity getPayLoad(String data){
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<String> entity = new HttpEntity<String>(data,headers);
        return entity;
    }
}

package cn.collin.service;


import cn.collin.entity.TestUtils;
import cn.collin.utils.JsonData;
import cn.collin.utils.PayLoadData;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by collin on 17-5-3.
 */
public class PostService {
    JsonData jsonData = new JsonData();

    @Autowired
    RestTemplate restTemplate;

    private PayLoadData payLoadData;

    //kafka URL
    private String kafkaURL = "";

    //chaincode serverURL
    private String url = "";
    //test frequency
    private int testFrq = 0;

    //payload to post
    private String payLoad = "localhost:8080/hello";
    private String args = "";//args in payload
    private String chaincodeID = "";//chaincodeID in payload

    private OutputStreamWriter out = null;
    private BufferedReader reader = null;
    private String response="";

    //sendTS --- send timestamp
    //recvTS --- receive timestamp
    String sendTS = "";
    String recvTS = "";

    int interval = 0;
    int fstLetter = (int)'a';

    SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss|SSS");

    //post request
    public  String sendPost(TestUtils testUtils, String CCID) {
        this.url = testUtils.getTestUrl();
        this.testFrq = testUtils.getTestFrq();
        System.out.println("testFrq = [" + testFrq + "], CCID = [" + CCID + "]");
        interval = 1000/testFrq;
        if(testFrq >= 1000 || testFrq == 0){

            //the post frequency < 1000
            return "test fail";
        } else {
            try {

                //connect the server
                URL httpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//no cache
                conn.setInstanceFollowRedirects(true);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                //post request
                out = new OutputStreamWriter(
                        conn.getOutputStream());
                for(int i=0; i<testFrq; i++) {
                    //a->b b->c c->d d->e e->a; invoke_value : 10
                    String invokeA = (char) (fstLetter + i % 5) + "";
                    String invokeB = "";
                    if(invokeA .equals("e")){
                        invokeB = "a";
                    }else {
                        invokeB = (char)(fstLetter + 1 + i%5) + "";
                    }
                    //form the data to post
                    args = "[" + "\"" + invokeA + "\"" + "," + "\"" + invokeB + "\"" + "," + "\"" + "10" + "\"" + "]";
                    chaincodeID = "\"name\":\"" + CCID + "\"";
                    payLoad = jsonData.getJsonData("invoke", chaincodeID, "invoke", args, "3");
                    System.out.println("payload:"+payLoad);

                    //post the data to chaincode server
                    out.write(payLoad);
                    out.flush();

                    //post the send time to kafka
                    //to complete
                    sendTS = formatter.format(new Date(System.currentTimeMillis()));
                    System.out.println("sendTime:"+sendTS);
                    /*HttpEntity<String> entity = payLoadData.getPayLoad(sendTS);
                    Object object = restTemplate.postForObject(kafkaURL, entity, String.class);*/

                    //record the response
                    reader = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String lines = "";
                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), "utf-8");
                        response+=lines;
                    }

                    //see whether the response is null and record the receive time
                    if (response.equals(null)) {
                        response = "no response";
                    } else {

                        //status --- show whether the invoke succeed
                        //invokeId --- invokeId
                        recvTS = formatter.format(new Date(System.currentTimeMillis()));
                        System.out.println("receiveTime:"+recvTS);
                        String result = JSONObject.fromObject(response).getString("result");
                        String status = JSONObject.fromObject(result).getString("status");
                        String invokeId = JSONObject.fromObject(result).getString("message");
                        //post the receive and the result time to kafka
                        //to complete
                        System.out.println("status:"+status+"   invokeId:"+invokeId);
                    }


                    reader.close();
                    Thread.sleep(interval);
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //use finally to close the reader stream and out stream
            finally {
                try{
                    if(out!=null){
                        out.close();
                    }
                    if(reader!=null){
                        reader.close();
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            return response;
        }

    }
}

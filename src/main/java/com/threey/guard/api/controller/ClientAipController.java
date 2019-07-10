package com.threey.guard.api.controller;

import com.threey.guard.api.domain.AccountData;
import com.threey.guard.api.domain.Ads;
import com.threey.guard.api.domain.ApiResult;
import com.threey.guard.api.service.AsyncServcie;
import com.threey.guard.api.service.ClientApiService;
import com.threey.guard.base.service.CommonService;
import com.threey.guard.base.util.FileUtil;
import com.threey.guard.base.util.GETProperties;
import com.threey.guard.base.util.JsonUtil;
import com.threey.guard.base.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ClientAipController {

    private static final Logger log = Logger.getLogger(ClientAipController.class);
    private static String FILE_EXT = GETProperties.readValue("FILE_EXT");
    private static String SERVER_PATH = GETProperties.readValue("SERVER_PATH");

    private static String STATIC_PATH = GETProperties.readValue("STATIC_PATH");

    @Autowired
    private ClientApiService service;
    @Autowired
    private CommonService commonService;
    @Autowired
    private AsyncServcie asyncServcie;


    /**
     * 获取客户端更新信息
     * @param request
     * @return
     */
    @RequestMapping("/update.do")
    @ResponseBody
    public ApiResult<String> updateInfo(HttpServletRequest request){
        String deviceNo = request.getParameter("deviceNo");

        ApiResult<String> result = this.service.versonUpdate(deviceNo);
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }

    /**
     * 获取广告位信息
     * @param request
     * @return
     */
    @RequestMapping("/screen/ads.do")
    @ResponseBody
    public ApiResult<List<Ads>>  getAdsInfo(HttpServletRequest request){
        ApiResult<List<Ads>>result = this.service.getAds(request.getParameter("deviceNo"));
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }


    /**
     * 开门信息上传
     * @param request
     * @return
     */
    @RequestMapping(value = "/upload/infos.do",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ApiResult<String> uploadInfos(HttpServletRequest request){
        System.out.println(request.getParameterMap());
        long startTime=System.currentTimeMillis();   //获取开始时间
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>开门指令开始");
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>1");
        MultipartFile resFile = multiRequest.getFile("file");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>3");
        Map<String ,Object> fileMap = new HashMap<String, Object>();
        Map<String,Object> map = new HashMap<>();
        boolean checkFile=true;
        String reStr = "";
        String remark = "";
        try {
            if (testFile(resFile)) {
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>4");
                fileMap = saveFile(resFile, "openRecord");
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>5");
                checkFile=checkFile&&testMap(fileMap);
            } else {
                remark = "图片未获取到";
            }
            if (!checkFile) {
                reStr = "操作失败:文件类型不符合要求";
                log.info("上传资源不通过:"+reStr);
                remark = "上传资源不通过:"+reStr;
                //WebUtil.toPrint(response, reStr,"text/html");
            } else{
                map.put("ret",0);
                map.put("msg","成功");
                map.put("oldName",fileMap.get("resFileOrigName"));
                map.put("resFileDbPath",fileMap.get("resFileDbPath"));
                map.put("fileExt",fileMap.get("resFileExtNoDot"));
                remark = "成功";
            }
        } catch (Exception e) {
            e.printStackTrace();
            reStr = "操作失败:出现系统错误";
            log.info("上传资源不通过:"+reStr);
            remark = "上传资源不通过:"+reStr;
            //WebUtil.toPrint(response, reStr,"text/html");

        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>2");
        String mode = request.getParameter("mode");
        Map<String,Object> rmap = new HashMap<>();
        rmap.put("cardNo",request.getParameter("cardNo"));
        rmap.put("openType",mode);
        rmap.put("picture",map.get("resFileDbPath"));
        rmap.put("deviceId",request.getParameter("deviceNo"));
        rmap.put("remark",remark);
        if("4".equals(mode)){
            this.service.callRecord(rmap);//记录呼叫
            this.asyncServcie.sendWxMessage(request.getParameter("deviceNo"),request.getParameter("cardNo"),(String) map.get("resFileDbPath"));//推送呼叫记录
        }else {
            this.service.openRecord(rmap);
        }
        ApiResult<String>  re = new ApiResult<>();
        re.setCode(0);
        re.setMsg("success");

        log.info(JsonUtil.getJsonString(re));
        System.out.println(JsonUtil.getJsonString(re));
        return re;
    }

    /**
     * 告警信息上传
     * @param request
     * @return
     */
    @RequestMapping("/upload/warn.do")
    @ResponseBody
    public ApiResult<String> uploadWarnInfos(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        map.put("deviceId",request.getParameter("deviceNo"));
        map.put("warnType",request.getParameter("mode"));
        this.service.warnRecord(map);
        ApiResult<String>  re = new ApiResult<>();
        re.setCode(0);
        re.setMsg("success");
        log.info(JsonUtil.getJsonString(re));
        System.out.println(JsonUtil.getJsonString(re));
        return re;
    }

    /**
     * 验证临时密码
     * @param request
     * @return
     */
    @RequestMapping("/validate/pass.do")
    @ResponseBody
    public ApiResult<String> validatePass(HttpServletRequest request){

        ApiResult<String>  re = new ApiResult<>();
        re = this.service.getPwd(request.getParameter("deviceNo"),request.getParameter("pwd"));
        log.info(JsonUtil.getJsonString(re));
        System.out.println(JsonUtil.getJsonString(re));
        return re;
    }


    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @RequestMapping("/update/accounts.do")
    @ResponseBody
    public ApiResult<List<AccountData>> getAccounts(HttpServletRequest request){
        ApiResult<List<AccountData>> result =  this.service.getAccounts(request.getParameter("deviceNo"));
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @RequestMapping("/update/houses.do")
    @ResponseBody
    public ApiResult<List<AccountData>> getHouses(HttpServletRequest request){
        ApiResult<List<AccountData>> result =  this.service.getHouses(request.getParameter("deviceNo"));
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }

    @RequestMapping("/tel.do")
    @ResponseBody
    public ApiResult tel(HttpServletRequest request){
        ApiResult result =  this.service.getDeviceTel(request.getParameter("deviceNo"));
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }

    @RequestMapping("/call.do")
    @ResponseBody
    public ApiResult call(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        map.put("device",request.getParameter("deviceNo"));
        map.put("house",request.getParameter("houseNo"));
        ApiResult result =  this.service.deviceCall(map,1);//callNum客户端发起第一号码呼叫
        log.info(JsonUtil.getJsonString(result));
        System.out.println(JsonUtil.getJsonString(result));
        return result;
    }

    private boolean testFile(MultipartFile resFile){
        if(resFile==null || resFile.getSize()==0){
            return false;
        }
        return true;

    }

    private Map<String, Object> saveFile(MultipartFile resFile,String prv) throws Exception{
        Map<String, Object> reMap = new HashMap<String, Object>();
        String resFileOrigName,resFileExt,resFileExtNoDot,resFileOrigNameNoExt;
        resFileOrigName = resFile.getOriginalFilename().toLowerCase();
        resFileOrigNameNoExt=resFileOrigName.substring(0, resFileOrigName.lastIndexOf("."));
        resFileExt = resFileOrigName.substring(resFileOrigName.lastIndexOf("."), resFileOrigName.length());
        resFileExtNoDot = resFileOrigName.substring(resFileOrigName.lastIndexOf(".")+1, resFileOrigName.length());
        boolean type = checkExt(resFileExtNoDot);
        if (!type) {
            reMap.put("error", "文件格式不对");
            return reMap;
        }
        log.info("上传资源文件大小:"+ FileUtil.formatFileSize(resFile.getSize()));
        log.info("上传资源文件源名称:"+resFileOrigName);
        log.info("上传资源文件源格式:"+resFileExt);
        String serverPath = SERVER_PATH;
        // String tempPath = ManageStatistic.MANAGE_RES_TEMPPATH_URL;
        String staticPath = STATIC_PATH;
        String curDateStr = StringUtil.getCurrDateStr();//时间格式yyyymmdd
        long resId = commonService.getNextVal("open_record");
        //资源文件路径
        String tempResFileName = prv+resId+resFileExt;
        String fileResTempFullDir = serverPath+staticPath+resFileExtNoDot+"/"+curDateStr+"/";
        String fileResTempFullPath = fileResTempFullDir+tempResFileName;
        log.info("资源文件服务器存放地址:"+fileResTempFullPath);
        String resFileDbPath = staticPath+resFileExtNoDot+"/"+curDateStr+"/"+tempResFileName;
        log.info("资源文件数据库存放地址:"+resFileDbPath);
        log.info("");

        //资源文件存储
        File tempResFileDir = new File(fileResTempFullDir);
        if(!tempResFileDir.exists()){
            tempResFileDir.mkdirs();
        }
        FileOutputStream resFileOutStream = null;
        InputStream resFileInPutStream = null;
        try {
            resFileOutStream = new FileOutputStream(fileResTempFullPath);
            resFileInPutStream = resFile.getInputStream();
            FileUtil.copyStream(resFile.getInputStream(), resFileOutStream, -1);
        } catch (Exception e) {
            throw e;
        }finally{
            IOUtils.closeQuietly(resFileInPutStream);
            IOUtils.closeQuietly(resFileOutStream);
        }
        log.info("resFileDbPath>>>>>>>>>>>>>>>"+resFileDbPath);
        log.info("resFileOrigName>>>>>>>>>>>>>>>"+resFileOrigName);
        log.info("resFileExtNoDot>>>>>>>>>>>>>>>"+resFileExtNoDot);
        log.info("fileResFullPath>>>>>>>>>>>>>>>"+fileResTempFullPath);
        reMap.put("ret",0);
        reMap.put("msg","成功");
        reMap.put("resFileDbPath",resFileDbPath);
        reMap.put("resFileOrigName",resFileOrigName);
        reMap.put("resFileExtNoDot",resFileExtNoDot);
        return reMap;

    }

    private  boolean checkExt(String ext){
        if (null==ext) {
            return false;
        }
        ext = ext.toUpperCase();
        String fM = FILE_EXT.toUpperCase();
        if (fM.indexOf(ext)>0) {
            return true;
        }
        return false;
    }

    private boolean testMap(Map<String, Object> map){
        return null == map.get("error");
    }
}

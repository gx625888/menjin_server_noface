package com.threey.guard.manage.controller;

import com.baidu.aip.face.AipFace;
import com.threey.guard.base.domain.DataTable;
import com.threey.guard.base.domain.ManagerUser;
import com.threey.guard.base.service.CommonService;
import com.threey.guard.base.util.Constants;
import com.threey.guard.base.util.GETProperties;
import com.threey.guard.base.util.XlsUtil;
import com.threey.guard.manage.domain.BuildUnit;
import com.threey.guard.manage.domain.Card;
import com.threey.guard.manage.domain.Person;
import com.threey.guard.manage.domain.QueryResidentailByPerson;
import com.threey.guard.manage.service.ManagerCardService;
import com.threey.guard.manage.service.ManagerPersonService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/person")
public class ManagerPersonController {

    private Logger log = Logger.getLogger(ManagerResourceController.class);
	
    private Logger logger = Logger.getLogger(ManagerPersonController.class);
    @Autowired
    private ManagerPersonService managerPersonService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ManagerCardService managerCardService;
    
    private static String FILE_EXT = GETProperties.readValue("FILE_EXT");

	private StringBuilder persongroup = new StringBuilder();

    @RequestMapping("/toPersonList.shtml")
    public String toManagerUserList(HttpServletRequest request){
        if (null!=request.getParameter("houseId")){
            request.setAttribute("houseId",request.getParameter("houseId"));
            return "manage/person/personIndexHouse";
        }

        return "manage/person/personIndex";
    }

    @RequestMapping(value = "/page.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DataTable.Resp<Person> page(DataTable.Req p , HttpServletRequest request){
        String houseId = request.getParameter("houseId");
        ManagerUser loginUser = (ManagerUser) request.getSession().getAttribute(Constants.SessionKey.LOGIN_USER);
        if (null==houseId){
            Map<String,Object> map = new HashMap<String,Object>();
            if(loginUser.getManagerType() != 0){
                map.put("createUserCompany",loginUser.getManagerCompany());
                if(loginUser.getManagerType() == 2){
                    map.put("createUserProvince",loginUser.getManagerProvince());
                }
                if(loginUser.getManagerType() == 3){
                    map.put("createUserCity",loginUser.getManagerCity());
                }
                if(loginUser.getManagerType() == 4){
                    map.put("createUserResidentail",loginUser.getManagerResidentail());
                }
            }
            map.put("name",request.getParameter("p_name"));
            map.put("phone",request.getParameter("p_phone"));
            map.put("cardNo",request.getParameter("p_cardNo"));
            map.put("bandStatus",request.getParameter("p_bandStatus"));
            map.put("house",request.getParameter("house"));
            List<Person> persons = managerPersonService.list(map,p.getPage()-1,p.getLimit());
            int count = this.managerPersonService.count(map);
            return new DataTable.Resp<Person>(persons,count,0);
        }else {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("houseId",houseId);
            List<Person> persons = managerPersonService.list(map,p.getPage()-1,Integer.MAX_VALUE);
            return new DataTable.Resp<Person>(persons,persons.size(),0);
        }

    }

    @RequestMapping("/toAdd.shtml")
    public String toAdd(HttpServletRequest request){
        String id = request.getParameter("id");
        if (id!=null){
            Person person = this.managerPersonService.getOne(id);
            request.setAttribute("person",person);
            request.setAttribute("opt","edit");
        }
        if (null!=request.getParameter("houseId")){
            request.setAttribute("houseId",request.getParameter("houseId"));
            request.setAttribute("unitId",request.getParameter("pid"));
        }
        return "manage/person/personAdd";
    }

    @RequestMapping(value = "/save.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> saveOrUpdate(Person person,HttpServletRequest request){
        ManagerUser loginUser = (ManagerUser) request.getSession().getAttribute(Constants.SessionKey.LOGIN_USER);
        if (StringUtils.isNotEmpty(person.getId())){
            this.managerPersonService.update(person);

        }else{
            long id = this.commonService.getNextVal("mj_person");
            person.setId(String.valueOf(id));
            person.setCreateUser(loginUser.getMid());
            this.managerPersonService.add(person);
            if (null!=request.getParameter("houseId")){//保存居住 House关系
                Map<String,Object> params = new HashMap<>();
                params.put("houseId",request.getParameter("houseId"));
                params.put("personId",person.getId());
                this.managerPersonService.saveHousePerson(params);
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }

    @RequestMapping(value = "/del.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> delete(HttpServletRequest request){
        String id = request.getParameter("id");
        this.managerPersonService.del(id);
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }

    @RequestMapping(value = "/uploadFile.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> uploadFile(HttpServletRequest request, HttpServletResponse response){
        ManagerUser loginUser = (ManagerUser) request.getSession().getAttribute(Constants.SessionKey.LOGIN_USER);
        long startTime=System.currentTimeMillis();   //获取开始时间
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
        MultipartFile resFile = multiRequest.getFile("file");
        String[] arr = {"name","phone","sex","cardType","cardNo","birthday","country","nation","type","address"};
        List<Map<String,Object>> list = XlsUtil.analysisXls(arr,resFile);
        for (Map<String,Object> map:list){
            map.put("createUser",loginUser.getMid());
            map.put("id",this.commonService.getNextVal("mj_person")+"");
            if("女".equals(map.get("sex"))){
                map.put("sex",1);
            }else{
                map.put("sex",2);
            }
            if("身份证".equals(map.get("cardType"))){
                map.put("cardType",1);
            } else if ("护照".equals(map.get("cardType"))){
                map.put("cardType",2);
            } else if ("军人证".equals(map.get("cardType"))){
                map.put("cardType",3);
            } else {
                map.put("cardType",4);
            }
            if("非关注人群".contains((String)map.get("type"))){
                map.put("type",1);
            } else if ("独居老人".contains((String)map.get("type"))){
                map.put("type",2);
            } else if ("残障人士".contains((String)map.get("type"))){
                map.put("type",3);
            } else if ("涉案人员".contains((String)map.get("type"))){
                map.put("type",4);
            } else if ("涉毒人员".contains((String)map.get("type"))){
                map.put("type",5);
            } else if ("涉黑人员".contains((String)map.get("type"))){
                map.put("type",6);
            } else {
                map.put("type",1);
            }
        }
        this.managerPersonService.batchInsert(list);
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功导入"+list.size()+"条数据！");
        return map;
    }

    @RequestMapping(value = "/ResidentailPage.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DataTable.Resp<QueryResidentailByPerson> ResidentailPage(DataTable.Req p , HttpServletRequest request){
        String id = request.getParameter("id");
        List<QueryResidentailByPerson> list = managerPersonService.queryResidentailByPerson(id,p.getPage()-1,p.getLimit());
        int count = this.managerPersonService.countResidentailByPerson(id);
        return new DataTable.Resp<QueryResidentailByPerson>(list,count,0);
    }

    @RequestMapping(value = "/CardPage.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DataTable.Resp<Card> CardPage(DataTable.Req p , HttpServletRequest request){
        String id = request.getParameter("id");
        List<Card> list = managerPersonService.queryCardByPerson(id,p.getPage()-1,p.getLimit());
        int count = this.managerPersonService.countCardByPerson(id);
        return new DataTable.Resp<Card>(list,count,0);
    }

    @RequestMapping("/toPersonDetail.shtml")
    public String toPersonDetail(HttpServletRequest request){
        String id = request.getParameter("id");
        Person person = this.managerPersonService.getOne(id);
        request.setAttribute("person",person);
        return "manage/person/personDetailIndex";
    }

    @RequestMapping(value = "/cancelBand.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> cancelBand(HttpServletRequest request){
        Map<String,Object> pMap = new HashMap<>();
        pMap.put("person",request.getParameter("person"));
        pMap.put("house",request.getParameter("house"));
        this.managerPersonService.deleteBandInfo(pMap);
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }

    @RequestMapping(value = "/cardLoseStatus.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> cardLoseStatus(HttpServletRequest request){
        this.managerCardService.toLoseCard(request.getParameter("id"));
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }

    @RequestMapping(value = "/saveHousePerson.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> saveHousePerson(HttpServletRequest request){
        Map<String,Object> pMap = new HashMap<>();
        pMap.put("houseId",request.getParameter("house"));
        pMap.put("personId",request.getParameter("personId"));
        pMap.put("liveType","");
        this.managerPersonService.saveHousePerson(pMap);
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }
    
	/**
	 * 单个居民的照片上传页面
	 * @param request
	 * @return
	 */
    @RequestMapping("/personAddFace.shtml")
    public String personAddFace(HttpServletRequest request) {
        String person = request.getParameter("person");
        String phone = request.getParameter("phone");
        request.setAttribute("person", person);
        request.setAttribute("phone", phone);
        return "manage/person/personAddFace";
    }
    
    /**
     * 上传人脸识别照片
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/uploadFaceimage.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> uploadFaceimage(HttpServletRequest request, HttpServletResponse response){
        long startTime=System.currentTimeMillis();   //获取开始时间
        Map<String,Object> map = new HashMap<>();
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
        String person = request.getParameter("person");
        String phone = request.getParameter("phone");
        String simage = null;
        
        //获取上传的图片
        MultipartFile resFile = multiRequest.getFile("file");
        String resFileOrigName = resFile.getOriginalFilename().toLowerCase();
        String resFileExtNoDot = resFileOrigName.substring(resFileOrigName.lastIndexOf(".")+1, resFileOrigName.length());
        String formatfilename = phone + "." + resFileExtNoDot;

        //验证图片格式，转码图片
        if(checkExt(resFileExtNoDot)) {
    		try {
        	    byte[] filebytes = resFile.getBytes();
				simage = fileToBase64(filebytes);
	            log.info("转码后图片大小："+simage.length());
                map.put("oldName",resFileOrigName);
	            log.info("上传资源文件名："+resFileOrigName);
                map.put("fileExt",resFileExtNoDot);
	            log.info("上传资源文件格式："+resFileExtNoDot);
			}catch (Exception e) {
	            e.printStackTrace();
	            log.info("上传资源不通过:转码失败");
	            //WebUtil.toPrint(response, reStr,"text/html");
	            map.put("ret",-1);
	            map.put("msg","转码失败");
	            return map;
			}
        }else {
            map.put("ret",-1);
            map.put("msg","文件格式错误");
            log.info("文件格式错误");
            return map;
        }
        
		//TODO--百度人脸识别API---新增人脸照片
        AipFace baiduclient = baiDuFace();
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info", person+":"+phone);
        options.put("action_type", "APPEND");
        
        //获取用户楼栋单元
        List<BuildUnit> unitperson =  this.managerPersonService.queryUnitByPerson(person);
        for(int i=0;i<unitperson.size();i++) {
        	BuildUnit buildunit = unitperson.get(i);
        	persongroup.append(buildunit.getResidentailId()).append("_");
        	persongroup.append(buildunit.getParentId()).append("_");
        	persongroup.append(buildunit.getId());
        	log.info("用户所属人脸库："+persongroup.toString());
            // 添加人脸图片
            JSONObject res = baiduclient.addUser(simage, "BASE64", persongroup.toString(), phone, options);
            int error_code = res.getInt("error_code");
            if(error_code == 0) {
            	String face_token = res.getString("face_token");
                map.put("ret",0);
            	map.put("msg", "用户所属人脸库："+persongroup.toString()+"-文件上传成功！");
            }else {
            	map.put("ret",-1);
            	map.put("msg", "文件上传失败！用户所属人脸库："+persongroup.toString()+"-"+error_code);
            }
            log.info(res.toString());
            log.info(res.getInt("error_code")+"-------"+res.getString("error_msg"));
        	persongroup.setLength(0);
        }
		return map;
    }
    
    /**
     * 验证图片格式
     * @param resFile
     * @return
     */
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
    
    /**
     * 将图片转换成Base64编码的字符串
     * @param file
     * @return
     */
    public String fileToBase64(byte[] filebytes) {
        String base64 = null;
        base64 = Base64.getEncoder().encodeToString(filebytes);
        base64 = base64.replaceAll("[\\s*\t\n\r]", "");
        return base64;
    }

    /**
     * 删除用户照片
     * @param request
     * @return
     */
    @RequestMapping(value = "/delface.shtml",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Map<String,Object> deleteFace(HttpServletRequest request){
    	Person person = new Person();
    	person.setId(request.getParameter("person"));
        this.managerPersonService.update(person);
        Map<String,Object> map = new HashMap<>();
        map.put("ret",0);
        map.put("msg","成功");
        return map;
    }
    
	/**
	 * 批量居民的照片上传页面
	 * @param request
	 * @return
	 */
    @RequestMapping("/personAddFaceZip.shtml")
    public String personAddFaceZip(HttpServletRequest request) {
        String residentail = request.getParameter("residentail");
        request.setAttribute("residentail", residentail);
        return "manage/person/personAddFaceZip";
    }
    
    public AipFace baiDuFace() {
    	//设置APPID/AK/SK
        final String APP_ID = "16395390";
        final String API_KEY = "IRtHMRUh6K4wKYnGMmCNxe4r";
        final String SECRET_KEY = "5NV9noGGc4PP3GRDxB5OxdCgGgzoTvfk";
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
        return client;
    }

    
}

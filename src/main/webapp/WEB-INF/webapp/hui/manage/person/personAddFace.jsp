<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE HTML>
<html>
<head>
    <link rel="stylesheet" href="${ctx}/ui/lib/layui/css/layui.css" media="all">
    <script type="text/javascript" src="${ctx}/ui/lib/jquery/1.9.1/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/script/My97DatePicker/WdatePicker.js"></script>
    <title>添加居民照片</title>
</head>
<body>

<form style="margin-top: 20px" class="layui-form" action="" enctype="multipart/form-data" lay-filter="resourceForm">
    <div class="layui-form-item">
        <input type="hidden" name ="person" id="person" value = "${person}" >
        <label class="layui-form-label">手机号：</label>
        <div class="layui-input-block">
            <input type="text" id="phone" name="phone" required style="width: 200px" value="${phone}" lay-verify="required" autocomplete="off" class="layui-input" readonly>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">上传照片：</label>
        <button type="button" class="layui-btn" id="selfaceimage">
            <i class="layui-icon">&#xe67c;</i>选择文件
        </button>
    	<div class="layui-upload-list">
		    <img class="layui-upload-img" id="faceimage" style="margin-left:150px;height: 400px;width:350px;">
		</div>
        <span id="fileName"></span>
        <input hidden="true" id="filePath" name="filePath"/>
        <input hidden="true" id="fileExtension" name="fileExtension"/>
    </div>
    <div class="layui-form-item">
        <div class="layui-input-block">
            <button class="layui-btn" id="uplfaceimage">上传</button>
            <button type="reset" class="layui-btn layui-btn-primary" onclick="cancel()">取消</button>
        </div>
    </div>
</form>
<script type="text/javascript" src="${ctx}/ui/lib/layui/layui.all.js"></script>
<script>
    //Demo
    var formobj;
    
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
    }
    
    layui.use('upload', function(){
        var upload = layui.upload;
        var form = layui.form;
        var person = $("#person").val();
        var phone = $("#phone").val();
        formobj = form;
        //执行实例
        var uploadInst = upload.render({
        	method: 'post'
            ,elem: '#selfaceimage' //“选择文件”按钮的ID
            ,url: '${ctx}/person/uploadFaceimage.shtml' //上传接口
			,data: {person: person,phone: phone} //传递到后台的数据
            ,accept: 'file' //允许上传的文件类型
            ,exts:  'jpg|png|jpeg' //设置智能上传图片格式文件
            //,size: 500000 //最大允许上传的文件大小
            ,multiple: false //设置是否多个文件上传
            ,auto: false //不自动上传设置
            ,bindAction: '#uplfaceimage' //“上传”按钮的ID
            ,choose: function(obj){
                //预读本地文件示例，不支持ie8
                obj.preview(function(index, file, result){
                  $('#faceimage').attr('src', result); //存入src完成预览
                });
              }
            ,done: function(res){
                if(res.ret==0){
                    $("#fileName").val(res.oldName);
                    $("#filePath").val(res.resFileDbPath);
                    $("#fileExtension").val(res.fileExt);
                    //formobj.render();
                    parent.layer.msg("上传成功!");
                    var index = parent.layer.getFrameIndex(window.name);
                    parent.tableObj.reload();
                    parent.layer.close(index);
                }else if(res.ret==-1){
                    layer.msg(res.oldName+'照片上传'+res.msg);
                }
            }
            ,error: function(){
                //请求异常回调
                layer.msg("上传失败!");
            }
        });
    });
</script>

</body>
</html>
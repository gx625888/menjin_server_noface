<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE HTML>
<html>
<head>
    <link rel="stylesheet" href="${ctx}/ui/lib/layui/css/layui.css" media="all">
    <script type="text/javascript" src="${ctx}/ui/lib/jquery/1.9.1/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/script/My97DatePicker/WdatePicker.js"></script>
    <title>快速绑定</title>
</head>
<body>

<form style="margin-top: 20px" class="layui-form" action="" lay-filter="cardForm">
    <div class="layui-form-item">
        <input type="hidden" name ="person" value = "${person}" >
        <label class="layui-form-label"><label style="color: red">*</label>卡片编号：</label>
        <div class="layui-input-block">
            <input type="text" id="cardId" name="cardId" required style="width: 200px" lay-verify="required" placeholder="卡片编号" autocomplete="off" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <div class="layui-input-block">
            <button class="layui-btn" lay-submit lay-filter="formDemo">确认</button>
            <button type="reset" class="layui-btn layui-btn-primary" onclick="cancel()">取消</button>
        </div>
    </div>
</form>
<script type="text/javascript" src="${ctx}/ui/lib/layui/layui.all.js"></script>
<script>
    //Demo
    var formobj;
    var opt = '${opt}';
    layui.use('form', function(){
        var form = layui.form;
        formobj = form;
        //监听提交
        form.on('submit(formDemo)', function(data){
            $.ajax({
                type:"post",
                url :"${ctx}/card/baseTxAddBindInfo.shtml",
                data:data.field,
                success:function(data){
                    if (data.ret==0){
                        parent.parent.layer.msg("操作成功!");
                        var index = parent.parent.layer.getFrameIndex(window.name);
                        parent.parent.tableObj.reload();
						$("#cardId").val();
                        //parent.parent.layer.close(index);
                    } else{
                        layer.msg(data.msg);
                    }
                },
                error:function(){
                    layer.msg("服务器开小差了!");
                }
            });

            return false;
        });

    });
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name);
        parent.layer.close(index);
    }
</script>

</body>
</html>
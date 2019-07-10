<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE HTML>
<html>
<head>
    <%@ include file="../../common/resource.jsp" %>
    <title>小区管理</title>
</head>
<body>
<nav class="breadcrumb">
    <i class="Hui-iconfont">&#xe67f;</i> 首页
    <span class="c-gray en">&gt;</span> 社区服务
    <span class="c-gray en">&gt;</span> 居民管理
    <a class="btn btn-success radius r" style="line-height:1.6em;margin-top:3px" href="javascript:location.replace(location.href);" title="刷新" >
        <i class="Hui-iconfont">&#xe68f;</i></a></nav>
<div class="page-container">
    <div class="cl pd-5 bg-1 bk-gray">
        <label>居民姓名：</label>
        <input type="text" class="input-text" style="width:150px;margin-left: 4px" placeholder="输入居民姓名" id="p_name" name="p_name">
        <label>手机号码：</label>
        <input type="text" class="input-text" style="width:150px;margin-left: 4px" placeholder="输入手机号码" id="p_phone" name="p_phone">
        <label>证件号码：</label>
        <input type="text" class="input-text" style="width:150px;margin-left: 4px" placeholder="输入证件号码" id="p_cardNo" name="p_cardNo">
        <%--<label>绑定状态：</label>
        <span class="select-box inline">
            <select name="p_bandStatus" id="p_bandStatus" class="select" style="width: 100px">
                <option value="">全部</option>
                <option value="0">未绑定</option>
                <option value="1">已绑定</option>
            </select>
        </span>--%>
        <button type="submit" onclick="query()" class="btn btn-success radius" id="queryBtn" name=""><i class="Hui-iconfont">&#xe665;</i> 查询</button>
        <div>
            <span class="l"> <%--<a href="javascript:;" onclick="datadel()" class="btn btn-danger radius">
            <i class="Hui-iconfont">&#xe6e2;</i> 批量删除</a>--%>
            <a class="btn btn-primary radius" href="javascript:;"
               onclick="add_person('添加居民','${ctx}/person/toAdd.shtml','450','550')">
                <i class="Hui-iconfont">&#xe600;</i> 添加居民</a> </span>
                <span class="l" style="margin-left: 5px">
               <a type="button" class="btn btn-primary radius" id="test1">
                批量导入
            </a>
            </span>
            <span class="l" style="margin-left: 5px">
               <a href="${ctx}/templet/person_templet.xlsx" type="button" class="btn btn-primary radius">
                下载模板
            </a>
            </span>

        </div>
    </div>
    <table id="dataTable" lay-filter="test" >

    </table>
</div>
<%@ include file="../../common/footer.jsp" %>

<script type="text/html" id="barDemo">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">删除</a>
    <a class="layui-btn layui-btn-xs" lay-event="uploadface">上传照片</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delface">删除照片</a>
</script>

<script type="text/javascript">
    var tableObj;
    layui.select
    layui.use('table', function(){
        var table = layui.table;
        tableObj = table.render({
            elem: '#dataTable'

            ,height: 'full-120'
            ,even:true
            ,url: '${ctx}/person/page.shtml' //数据接口
            ,page: true //开启分页
            ,method: 'post'
            ,cols: [[ //表头
                {field: 'id', title: 'ID', width:80, sort: false, fixed: 'left', hide:true}
                ,{field: 'name', title: '姓名', width:150,templet:'<div><a style="color:blue" onclick="toPersonDetail({{ d.id}})">{{d.name}}</a></div>'}
                ,{field: 'sex', title: '性别', width:150,templet:"#r_sex"}
                ,{field: 'nation', title: '民族', width:150}
                ,{field: 'phone', title: '手机', width:150}
                ,{field: 'cardType', title: '证件类型', width:150,templet:"#r_cardType"}
                ,{field: 'cardNo', title: '证件编号', width:150}
                ,{field: 'wealth', title: '操作', width: 260, sort: false,toolbar:'#barDemo'}
            ]]

        });
        table.on('tool(test)', function(obj){ //注：tool是工具条事件名，test是table原始容器的属性 lay-filter="对应的值"
            var data = obj.data; //获得当前行数据
            var layEvent = obj.event; //获得 lay-event 对应的值（也可以是表头的 event 参数对应的值）
            var tr = obj.tr; //获得当前行 tr 的DOM对象
            if(layEvent === 'detail'){ //查看
                //do somehing
            } else if(layEvent === 'del'){ //删除
                layer.confirm('确认删除?', function(index){
                    $.ajax({
                        type:"post",
                        url :"${ctx}/person/del.shtml",
                        data:{id:obj.data.id},
                        success:function(data){
                            layer.close(index);
                            if (data.ret==0){
                                layer.msg("操作成功!")
                                var curr = tableObj.config.page.curr;
                                var total = tableObj.config.page.count;
                                var limit = tableObj.config.page.limit;
                                var currCount = total%limit;
                                if(curr>1){
                                    if (currCount>1){
                                        tableObj.reload();
                                    }else {
                                        tableObj.reload({page:{curr:curr-1}});
                                    }
                                }else {
                                    tableObj.reload();
                                }
                            } else{
                                layer.msg("服务器开小差了!");
                            }
                        },
                        error:function(){
                            layer.close(index);
                            layer.msg("服务器开小差了!");
                        }
                    });


                });
            } else if(layEvent === 'edit'){ //编辑
                layer_show("编辑",'${ctx}/person/toAdd.shtml?id='+obj.data.id,'450','550');

                /*  //同步更新缓存对应的值
                  obj.update({
                      name: '123'
                      ,title: 'xxx'
                  });*/
            } else if(layEvent === 'uploadface'){ //上传照片
            	var phone = obj.data.phone;
            	if(phone!=null){
            		if(phone!=''){
                        layer_show("上传照片",'${ctx}/person/personAddFace.shtml?person='+obj.data.id+'&phone='+phone,'650','750');
            		}
            	}else{
            		layer.msg("请先填写手机号！")
            	}
            } else if(layEvent === 'delface'){ //删除照片
                layer.confirm('确认删除?', function(index){
                    $.ajax({
                        type:"post",
                        url :"${ctx}/person/delface.shtml",
                        data:{person:obj.data.id},
                        success:function(data){
                            layer.close(index);
                            if (data.ret==0){
                                layer.msg("操作成功!")
                                var curr = tableObj.config.page.curr;
                                var total = tableObj.config.page.count;
                                var limit = tableObj.config.page.limit;
                                var currCount = total%limit;
                                if(curr>1){
                                    if (currCount>1){
                                        tableObj.reload();
                                    }else {
                                        tableObj.reload({page:{curr:curr-1}});
                                    }
                                }else {
                                    tableObj.reload();
                                }
                            } else{
                                layer.msg("服务器开小差了!");
                            }
                        },
                        error:function(){
                            layer.close(index);
                            layer.msg("服务器开小差了!");
                        }
                    });
                });
            }
        });
    });


    function query(){
        var params = {p_name:$('#p_name').val(),
            p_phone:$('#p_phone').val(),
            p_cardNo:$('#p_cardNo').val()
        };
        tableObj.reload({where:params,page:{curr:1}});
    }
    /*添加*/
    function add_person(title,url,w,h){
        layer_show(title,url,w,h);
    }
    /*编辑*/
    function person_edit(title,url,id,w,h){
        layer_show(title,url,w,h);
    }
    /*删除*/
    function person_del(obj,id){
        layer.confirm('角色删除须谨慎，确认要删除吗？',function(index){
            $.ajax({
                type: 'POST',
                url: '',
                dataType: 'json',
                success: function(data){
                    $(obj).parents("tr").remove();
                    layer.msg('已删除!',{icon:1,time:1000});
                },
                error:function(data) {
                    console.log(data.msg);
                },
            });
        });
    }
    layui.use('upload', function(){
        var upload = layui.upload;

        //执行实例
        var uploadInst = upload.render({
            elem: '#test1' //绑定元素
            ,url: '${ctx}/person/uploadFile.shtml' //上传接口
            ,accept: 'file'
            ,exts:  'xlsx|xls'
            ,before: function () {
                parent.layer.msg("正在上传，请稍后。。");
            }
            ,done: function(res){
                if(res.ret==0){
                    parent.layer.msg(res.msg);
                    //formobj.render();
                    var params = {p_name:$('#p_name').val(),
                        p_phone:$('#p_phone').val(),
                        p_cardNo:$('#p_cardNo').val(),
                        p_bandStatus:$('#p_bandStatus').val()
                    };
                    tableObj.reload({where:params});
                }
            }
            ,error: function(){
                //请求异常回调
                layer.msg("上传失败!");
            }
        });
    });
    function toPersonDetail(id) {
        var index =  layer.open({
            type: 2,
            area: [900+'px', 650+'px'],
            fix: false, //不固定
            maxmin: true,
            shade:0.4,
            content: '${ctx}/person/toPersonDetail.shtml?id='+id
        });
        layer.full(index);
    }
</script>
<script type="text/html" id="r_sex">
    {{# if(d.sex == 2){ }}男{{# }}}
    {{# if(d.sex == 1){ }}女{{# }}}
</script>
<script type="text/html" id="r_cardType">
    {{# if(d.cardType == 1){ }}身份证{{# }}}
    {{# if(d.cardType == 2){ }}护照{{# }}}
    {{# if(d.cardType == 3){ }}军人证{{# }}}
    {{# if(d.cardType == 4){ }}其他证件{{# }}}
</script>
</body>
</html>
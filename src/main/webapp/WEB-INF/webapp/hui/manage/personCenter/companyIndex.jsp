<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE HTML>
<html>
<head>
    <%@ include file="../../common/resource.jsp" %>
    <title>个人中心</title>
</head>
<body>
<nav class="breadcrumb">
    <i class="Hui-iconfont">&#xe67f;</i> 首页
    <span class="c-gray en">&gt;</span> 个人中心
    <span class="c-gray en">&gt;</span> 公司管理
    <a class="btn btn-success radius r" style="line-height:1.6em;margin-top:3px" href="javascript:location.replace(location.href);" title="刷新" >
        <i class="Hui-iconfont">&#xe68f;</i></a></nav>
<div class="page-container">
    <div class="cl pd-5 bg-1 bk-gray">
        <label>公司名称：</label>
        <input type="text" class="input-text" style="width:150px;margin-left: 4px" placeholder="输入公司名称" id="p_name" name="p_name">
        <button type="submit" onclick="query()" class="btn btn-success radius" id="queryBtn" name=""><i class="Hui-iconfont">&#xe665;</i> 查询</button>
        <div>
            <span class="l">
            <a class="btn btn-primary radius" href="javascript:;"
               onclick="add_person('添加公司','${ctx}/personCenter/companyToAdd.shtml','450','550')">
                <i class="Hui-iconfont">&#xe600;</i> 添加公司</a> </span>

        </div>
    </div>
    <table id="dataTable" lay-filter="test" >

    </table>
</div>
<%@ include file="../../common/footer.jsp" %>

<script type="text/html" id="barDemo">
    <a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">删除</a>
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
            ,url: '${ctx}/personCenter/companyPage.shtml' //数据接口
            ,page: true //开启分页
            ,method: 'post'
            ,cols: [[ //表头
                {field: 'id', title: 'ID', width:80, sort: false, fixed: 'left', hide:true}
                ,{field: 'companyName', title: '公司名称', width:150}
                ,{field: 'person', title: '负责人', width:150}
                ,{field: 'phone', title: '负责人电话', width:150}
                ,{field: 'adress', title: '公司地址', width:150}
                ,{field: 'wealth', title: '操作', width: 150, sort: false,toolbar:'#barDemo'}
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
                        url :"${ctx}/personCenter/companyDel.shtml",
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
                layer_show("编辑",'${ctx}/personCenter/companyToAdd.shtml?id='+obj.data.id,'450','550');

                /*  //同步更新缓存对应的值
                  obj.update({
                      name: '123'
                      ,title: 'xxx'
                  });*/
            }
        });
    });


    function query(){
        var params = {p_name:$('#p_name').val()
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

</script>
</body>
</html>
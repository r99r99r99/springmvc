/*//模拟placeholder插件
$(function() {
	$.fn.extend({
		placeholder: function() {
			if ('placeholder' in document.createElement('input')) {
				return this;
			} else {
				return this.each(function() {
					var _this = $(this),
						this_placeholder = _this.attr('placeholder'),
						$tip = $('<label class="name_lable">'+ this_placeholder +'</label>');
						 
					if (_this.val() !== '') {
						return;
					}
					$tip.insertAfter(_this);
					$tip.click(function() {
						_this.focus();
					});
					_this.keydown(function() {
						$tip.hide();
					}).blur(function() {
						if (_this.val().length === 0) {
							$tip.show();
						}
					});
				});
			}
		}
	});

	$('input[type="text"]').placeholder();
	$('input[type="password"]').placeholder();
});
*/
function getJSON(url,data, async,callback){
	$.ajax({
		type: "GET",
		url: url,
		data:data,
		cache: false,
		async:async,
		success: callback,
		dataType: "json"
	});
};

function wdateInit(obj,dateFmt){
	dateFmt=dateFmt||'yyyy-MM-dd HH:mm';
	obj.focus(function() {
		WdatePicker({
			onpicked:function(dp){
				$(this).val(dp.cal.getDateStr());
				$('#calendar').html("");
			},
			dateFmt:dateFmt
		});
	});	
};
//数据小数位补位
function formatNum(requestNum, bitmap) {
		if(requestNum==0){
			return 'NAN';
		}
		if ((requestNum+"").indexOf(".") > 0) {
			if ((bitmap+"").indexOf(".") > 0) {
					var bitarr = (bitmap+"").lastIndexOf(".");
					for (var i = 1; i < bitarr; i++) {
						requestNum += "0";
					}
			}
		}
return requestNum;
}
//开始时间要早于结束时间
function ifBeginLaterEnd(startDate,endDate){
	endDate = endDate.replace(/-/g,"/");
	startDate = startDate.replace(/-/g,"/");
	var end = new Date(endDate);
	var start = new Date(startDate);
	if(start>=end){
		alert("开始时间要早于结束时间");
		return true;
	}
	return false;
}

//隐藏div
function closeLayer(layer){
	var lay = document.getElementById(layer);
 	lay.style.display="none";
}
//显示div
function openLayer(layer){
	var lay = document.getElementById(layer);
 	lay.style.display="block";
}
//变换div的显示状态
function changeLayer(layer){
	var lay = document.getElementById(layer);
	if(lay.style.display=="block"){
  		 lay.style.display="none"
  	 }else{
  		 lay.style.display="block";
  	 }
}

/**************************************时间格式化处理************************************/
function dateFtt(fmt,date)   
{ //author: meizz   
  var o = {   
    "M+" : date.getMonth()+1,                 //月份   
    "d+" : date.getDate(),                    //日   
    "h+" : date.getHours(),                   //小时   
    "m+" : date.getMinutes(),                 //分   
    "s+" : date.getSeconds(),                 //秒   
    "q+" : Math.floor((date.getMonth()+3)/3), //季度   
    "S"  : date.getMilliseconds()             //毫秒   
  };   
  if(/(y+)/.test(fmt))   
    fmt=fmt.replace(RegExp.$1, (date.getFullYear()+"").substr(4 - RegExp.$1.length));   
  for(var k in o)   
    if(new RegExp("("+ k +")").test(fmt))   
  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
  return fmt;   
} 

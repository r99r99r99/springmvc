package com.sdocean.dictionary.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.PlotLine;
import com.sdocean.common.model.Result;
import com.sdocean.common.model.ZTreeModel;
import com.sdocean.dictionary.model.UnitGroupModel;
import com.sdocean.dictionary.model.UnitModel;
import com.sdocean.dictionary.model.WaterQualityStandardModel;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.role.model.RoleModel;
import com.sdocean.station.model.StationModel;
import com.sdocean.station.model.StationTypeModel;
import com.sdocean.users.model.SysUser;

@Component
public class WaterQualityStandardDao extends OracleEngine {
	
	/*
	 * 得到所有的水质标准的列表
	 */
	public List<WaterQualityStandardModel> getStandardList(WaterQualityStandardModel model){
		List<WaterQualityStandardModel> list = new ArrayList<WaterQualityStandardModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.item,d.title as indicatorName,a.standard_grade as standardId,b.value as standardName,");
		sql.append(" a.min_value,a.max_value,a.remarks,a.water_type as waterType,c.value as waterTypeName,");
		sql.append(" a.min,case a.min when 1 then '>=' else '>' end as minkey,a.max,case a.max when 1 then '<=' else '<' end as maxkey");
		sql.append(" from waterqualitystandard a,sys_public b,sys_public c,dm_indicator d");
		sql.append(" where a.standard_grade = b.classid and b.parentcode = '0007'");
		sql.append(" and a.water_type = c.classid and c.parentcode = '0005'");
		sql.append(" and a.item = d.code");
		//添加查询条件
		if(model!=null&&model.getItem()!=null&&model.getItem().length()>0){
			sql.append(" and a.item like '%").append(model.getItem()).append("%'");
		}
		
		//增加排序
		sql.append(" order by a.item,a.standard_grade");
		list = this.queryObjectList(sql.toString(), WaterQualityStandardModel.class);
		
		return list;
	}
	
	/*
	 * 保存水质标准的修改
	 */
	public Result saveQualityChange(WaterQualityStandardModel model){
		Result result = new Result();
		result.setDotype(result.UPDATE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("修改成功");
		//判断唯一性条件
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append("select count(1) from waterqualitystandard where id <> ").append(model.getId()).append(" and item = '").append(model.getId()).append("' and standard_grade = ").append(model.getStandardId());
		int cou = 0;
		try {
			cou = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断唯一性时错误");
			return result;
		}
		if(cou>0){
			result.setResult(result.FAILED);
			result.setMessage("违反唯一性原则");
			return result;
		}
		//判断是否数值重复
		//判断最大值是否有冲突
		String max = "1=0";
		if(model.getMax()==1){
			max = "1=1";
		}
		StringBuffer maxSql = new StringBuffer("");
		maxSql.append(" select count(1) from waterqualitystandard a");
		maxSql.append(" where item = '").append(model.getItem()).append("' and a.water_type = ").append(model.getWaterType()).append(" and a.id <>").append(model.getId());
		maxSql.append(" and case when a.min =1 and ").append(max).append(" then ").append(model.getMax_value()).append(">= a.min_value else ").append(model.getMax_value()).append("> a.min_value end");
		maxSql.append(" and case when a.max =1 and ").append(max).append(" then ").append(model.getMax_value()).append("<= a.max_value else ").append(model.getMax_value()).append("< a.max_value end");
		int maxcou=0;
		try {
			maxcou = this.queryForInt(maxSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断最大值时错误");
			return result;
		}
		if(maxcou>0){
			result.setResult(result.FAILED);
			result.setMessage("最大值生成有冲突");
			return result;
		}
		//判断最小值是否有冲突
		String min = "1=0";
		if(model.getMin()==1){
			min = "1=1";
		}
		StringBuffer minSql = new StringBuffer("");
		minSql.append(" select count(1) from waterqualitystandard a");
		minSql.append(" where item = '").append(model.getItem()).append("' and a.water_type = ").append(model.getWaterType()).append(" and a.id <>").append(model.getId());
		minSql.append(" and case when a.min =1 and ").append(min).append(" then ").append(model.getMin_value()).append(">= a.min_value else ").append(model.getMin_value()).append("> a.min_value end");
		minSql.append(" and case when a.max =1 and ").append(min).append(" then ").append(model.getMin_value()).append("<= a.max_value else ").append(model.getMin_value()).append("< a.max_value end");
		int mincou=0;
		try {
			mincou = this.queryForInt(minSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断最小值时错误");
			return result;
		}
		if(mincou>0){
			result.setResult(result.FAILED);
			result.setMessage("最小值生成有冲突");
			return result;
		}
		//开始数据操作
		StringBuffer sql = new StringBuffer("");
		sql.append("update waterqualitystandard set item=?,standard_grade=?,min_value=?,max_value=?,remarks=?,water_type=?,min=?,max=? where id=?");
		Object[] params = new Object[]{
			model.getItem(),model.getStandardId(),model.getMin_value(),model.getMax_value(),
			model.getRemarks(),model.getWaterType(),model.getMin(),model.getMax(),model.getId()
		};
		int res = 0;
		try {
			res = this.update(sql.toString(), params);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("保存数据时失败");
		}
		
		return result;
	}

	/*
	 * 保存水质标准的修改
	 */
	public Result saveNewQuality(WaterQualityStandardModel model){
		Result result = new Result();
		result.setDotype(result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("新增成功");
		//判断唯一性条件
		StringBuffer checkSql = new StringBuffer("");
		checkSql.append("select count(1) from waterqualitystandard where standard_grade = ").append(model.getStandardId()).append(" and item = '").append(model.getId()).append("'");
		int cou = 0;
		try {
			cou = this.queryForInt(checkSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断唯一性时错误");
			return result;
		}
		if(cou>0){
			result.setResult(result.FAILED);
			result.setMessage("违反唯一性原则");
			return result;
		}
		//判断是否数值重复
		//判断最大值是否有冲突
		String max = "1=0";
		if(model.getMax()==1){
			max = "1=1";
		}
		StringBuffer maxSql = new StringBuffer("");
		maxSql.append(" select count(1) from waterqualitystandard a");
		maxSql.append(" where item = '").append(model.getItem()).append("' and a.water_type = ").append(model.getWaterType());
		maxSql.append(" and case when a.min =1 and ").append(max).append(" then ").append(model.getMax_value()).append(">= a.min_value else ").append(model.getMax_value()).append("> a.min_value end");
		maxSql.append(" and case when a.max =1 and ").append(max).append(" then ").append(model.getMax_value()).append("<= a.max_value else ").append(model.getMax_value()).append("< a.max_value end");
		int maxcou=0;
		try {
			maxcou = this.queryForInt(maxSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断最大值时错误");
			return result;
		}
		if(maxcou>0){
			result.setResult(result.FAILED);
			result.setMessage("最大值生成有冲突");
			return result;
		}
		//判断最小值是否有冲突
		String min = "1=0";
		if(model.getMin()==1){
			min = "1=1";
		}
		StringBuffer minSql = new StringBuffer("");
		minSql.append(" select count(1) from waterqualitystandard a");
		minSql.append(" where item = '").append(model.getItem()).append("' and a.water_type = ").append(model.getWaterType());
		minSql.append(" and case when a.min =1 and ").append(min).append(" then ").append(model.getMin_value()).append(">= a.min_value else ").append(model.getMin_value()).append("> a.min_value end");
		minSql.append(" and case when a.max =1 and ").append(min).append(" then ").append(model.getMin_value()).append("<= a.max_value else ").append(model.getMin_value()).append("< a.max_value end");
		int mincou=0;
		try {
			mincou = this.queryForInt(minSql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("判断最小值时错误");
			return result;
		}
		if(mincou>0){
			result.setResult(result.FAILED);
			result.setMessage("最小值生成有冲突");
			return result;
		}
		//开始数据操作
		StringBuffer sql = new StringBuffer("");
		sql.append(" insert into waterqualitystandard(item,standard_grade,min_value,max_value,remarks,water_type,min,max) values(?,?,?,?,?,?,?,?)");
		Object[] params = new Object[]{
			model.getItem(),model.getStandardId(),model.getMin_value(),model.getMax_value(),
			model.getRemarks(),model.getWaterType(),model.getMin(),model.getMax()
		};
		int res = 0;
		try {
			res = this.update(sql.toString(), params);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("保存数据时失败");
		}
		
		return result;
	}
	
	/*
	 * 删除选中的水质等级
	 */
	public Result deleWaterQulity(WaterQualityStandardModel model){
		Result result = new Result();
		result.setDotype(result.DELETE);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(result.SUCCESS);
		result.setMessage("删除成功");
		
		StringBuffer sql = new StringBuffer("");
		sql.append(" delete from waterqualitystandard where id in (").append(model.getIds()).append(")");
		try {
			this.update(sql.toString(), null);
		} catch (Exception e) {
			result.setResult(result.FAILED);
			result.setMessage("删除失败");
		}
	
		return result;
	}
	
	/*
	 * 通过站点以及参数获得该参数的水质标准线
	 */
	public List<PlotLine> getPlotLines(StationModel station,String code){
		List<PlotLine> plotLines = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select b.remark as color,'solid' as dashStyle,a.max_value as value,");
		sql.append(" 2 as width,b.value as text,'right' as align,10 as x");
		sql.append(" from waterqualitystandard a,sys_public b");
		sql.append(" where a.item = '").append(code).append("'");
		sql.append(" and a.water_type =").append(station.getWaterType());
		sql.append(" and b.parentcode = '0007'");
		sql.append(" and a.standard_grade = b.classid");
		plotLines = this.queryObjectList(sql.toString(), PlotLine.class);
		return plotLines;
	}
	
	/*
	 * 通过站点查询出该站点参与水质评价的参数列表
	 */
	public List<IndicatorModel> getIndicator4StandByStation(StationModel station){
		List<IndicatorModel> list = new ArrayList<>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select distinct b.code,b.title,b.unitid,d.name as unitname,b.description,b.ordercode,b.standardcode");
		sql.append(" from waterqualitystandard a,dm_indicator b,view_stationid_deviceid_indicatorcode c,g_unit d");
		sql.append(" where a.water_type = ").append(station.getWaterType());
		sql.append(" and a.item = b.code");
		sql.append(" and b.code = c.indicatorcode and c.stationid = ").append(station.getId());
		sql.append(" and b.unitid = d.id");
		sql.append(" and b.isactive = 1");
		sql.append(" order by b.ordercode");
		list = this.queryObjectList(sql.toString(), IndicatorModel.class);
		return list;
	}
	
	/*
	 * 查询出站点,参数的标准水质等级
	 */
	public WaterQualityStandardModel getStandardByStationIndicator(StationModel station,IndicatorModel indicator){
		WaterQualityStandardModel stand = new WaterQualityStandardModel();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select id,item,standard_grade as standardid,min_value,max_value,remarks,water_type,min,max");
		sql.append(" from waterqualitystandard");
		sql.append(" where water_type = ").append(station.getWaterType());
		sql.append(" and standard_grade <=2");
		sql.append(" order by standard_grade desc limit 1");
		stand = this.queryObject(sql.toString(), WaterQualityStandardModel.class);
		return stand;
	}
	
	/*
	 * 根据站点和参数,获得水质等级配置集合
	 */
	public List<WaterQualityStandardModel> getStandardListByStationIndicator(StationModel station,IndicatorModel indicator){
		List<WaterQualityStandardModel> list = new ArrayList<WaterQualityStandardModel>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.id,a.item,a.standard_grade as standardid,a.min_value,a.max_value,a.remarks,a.water_type,a.min,a.max,b.color");
		sql.append(" from waterqualitystandard a,g_waterstandard_config b");
		sql.append(" where a.water_type = ").append(station.getWaterType());
		sql.append(" and a.standard_grade = b.classid");
		sql.append(" and b.typeid = ").append(station.getWaterType());
		sql.append(" and item = '").append(indicator.getCode()).append("'");
		list = this.queryObjectList(sql.toString(), WaterQualityStandardModel.class);
		return list;
	}
}

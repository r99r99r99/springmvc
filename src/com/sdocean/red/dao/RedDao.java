package com.sdocean.red.dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.aquacu.model.AquacuStatisModel;
import com.sdocean.common.model.Hcharts;
import com.sdocean.common.model.HchartsPie;
import com.sdocean.dataQuery.model.StatData;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataDao;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataModel;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.red.model.RedDetailModel;
import com.sdocean.red.model.RedStatisModel;
import com.sdocean.station.model.StationModel;

@Component
public class RedDao  extends OracleEngine{
	
	private int RedDomainId = 1;  //获得海水浴场的功能区ID
	
	@Resource
	MetadataTableDao tableDao;
	@Resource
	MetadataDao dataDao;

	/*
	 * 获得该站点的海水浴场等级
	 */
	public HchartsPie getRedDetailByStation(StationModel station) {
		HchartsPie pie = new HchartsPie();
		//查询海水浴场的水质评价等级
		List<Hcharts> levels = new ArrayList<Hcharts>();
		StringBuffer levelSql = new StringBuffer("");
		levelSql.append(" select code,name,remark,color,10 as y");
		levelSql.append(" from sys_domain_level");
		levelSql.append(" where domainid = ").append(RedDomainId);
		levelSql.append(" order by ordercode");
		levels = this.queryObjectList(levelSql.toString(), Hcharts.class);
		RedDetailModel bdm = new RedDetailModel();
		//查询海水浴场参与评价标准的参数
		StringBuffer indicaSql = new StringBuffer("");
		indicaSql.append(" select distinct indicatorcode as code");
		indicaSql.append(" from sys_domain_level a,sys_domain_threshold b");
		indicaSql.append(" where a.id = b.levelid");
		indicaSql.append(" and a.domainid = ").append(RedDomainId);
		indicaSql.append(" order by a.ordercode");
		List<IndicatorModel> indicators = this.queryObjectList(indicaSql.toString(), IndicatorModel.class);
		for(IndicatorModel indicator:indicators) {
			//初始化当前参数的游泳指数
			RedDetailModel indicatorDetail = new RedDetailModel();
			//获得海水浴场功能区中,该参数的配置信息
			List<RedDetailModel> redDetails = new ArrayList<RedDetailModel>();
			StringBuffer redSql = new StringBuffer("");
			redSql.append(" select distinct a.code as levelid,a.name as levelname,");
			redSql.append(" b.mincal,b.min,b.maxcal,b.max,indicatorcode");
			redSql.append(" from sys_domain_level a,sys_domain_threshold b");
			redSql.append(" where a.id = b.levelid");
			redSql.append(" and a.domainid =").append(RedDomainId);
			redSql.append(" and indicatorcode = '").append(indicator.getCode()).append("'");
			redSql.append(" order by a.ordercode ");
			redDetails = this.queryObjectList(redSql.toString(), RedDetailModel.class);
			if(redDetails==null||redDetails.size()<1) {
				continue;
			}
			///////////////////////////////读取该站点,该参数最后一条数据  ---从自动获取数据中读取
			//根据当前的时间,获得需要查询的表
			DateFormat beginDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			String nowDate = beginDf.format(calendar.getTime());
			MetadataTable table = tableDao.getOneTable(station, nowDate, 1);
			//根据站点参数以及查询的表格,得到最后一条记录
			MetadataModel dataModel = dataDao.getLastData4StationIndicator(station, indicator, table);
			if(dataModel==null||dataModel.getIndicator_code()==null||dataModel.getIndicator_code().length()<1) {
				calendar.add(Calendar.MONTH, -1);
				nowDate = beginDf.format(calendar.getTime());
				table = tableDao.getOneTable(station, nowDate, 1);
				dataModel = dataDao.getLastData4StationIndicator(station, indicator, table);
			}
			//////////////////////////////查询手动输入数据得到的功能区等级
			//设置查询手动输入的开始时间
			String begin = "";
			if(dataModel!=null&&dataModel.getCollect_time()!=null) {
				begin=dataModel.getCollect_time();
			}
			MetadataModel manualData = dataDao.getLastData4Manual(station, indicator,begin,nowDate);
			if(dataModel==null) {
				dataModel = manualData;
			}else {
				if(manualData!=null&&manualData.getData()!=null) {
					try {
						Date manuTime = beginDf.parse(manualData.getCollect_time());
						Date zidoTime = beginDf.parse(dataModel.getCollect_time());
						if(zidoTime.before(manuTime)) {
							dataModel = manualData;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			//当获取数据不为空时,判断该条数据符合标准
			if(dataModel!=null&&dataModel.getIndicator_code()!=null&&dataModel.getIndicator_code().length()>0&&dataModel.getData()!=null) {
				indicatorDetail = this.getRedDetailByLevel(redDetails, dataModel);
			}
			//当前参数的功能区等级与总体的功能区等级比较
			if(bdm==null) {
				bdm=indicatorDetail;
			}else {
				if(indicatorDetail.getLevelId()>bdm.getLevelId()) {
					bdm=indicatorDetail;
				}
			}
		}
		if(bdm!=null&&bdm.getLevelId()>0) {
			for(Hcharts H:levels) {
				if(bdm.getLevelId()==Integer.parseInt(H.getCode())) {
					H.setSliced(true);
					H.setSelected(true);
					pie.setTitle(H.getName());
					pie.setDatas(levels);
					break;
				}
			}
		}
		return pie;
	}
	
	/*
	 * 通过参数的功能区标准,以及当前数据,查询出功能区等级
	 */
	public RedDetailModel getRedDetailByLevel(List<RedDetailModel> redDetails,MetadataModel dataModel) {
		RedDetailModel result = new RedDetailModel();
		Double data = dataModel.getData();
		StringBuffer checkSql = new StringBuffer("");
		StringBuffer levelidSql = new StringBuffer(" case when 1= 0 then 0");
		StringBuffer levelNameSql = new StringBuffer(" case when 1=0 then '0'");
		for(RedDetailModel detail:redDetails) {
			String minCal = ">";
			String maxCal = "<";
			if(detail.getMinCal()==1) {
				minCal = ">=";
			}
			if(detail.getMaxCal()==1) {
				maxCal = "<=";
			}
			levelidSql.append(" when ").append(data).append(minCal).append(detail.getMin()).append(" and ").append(data).append(maxCal).append(detail.getMax()).append(" then ").append(detail.getLevelId());
			levelNameSql.append(" when ").append(data).append(minCal).append(detail.getMin()).append(" and ").append(data).append(maxCal).append(detail.getMax()).append(" then '").append(detail.getLevelName()).append("'");
		}
		levelidSql.append(" else 0 end as levelId");
		levelNameSql.append(" else '无效等级' end as levelName");
		checkSql.append(" select ").append(levelidSql).append(",").append(levelNameSql);
		result = this.queryObject(checkSql.toString(), RedDetailModel.class);
		return result;
	}
	
	
	/*
	 * 获得当前站点,统计口径,查询时间内的赤潮预警等级趋势
	 */
	public List<StatData> getRedStatDataSearchList(RedStatisModel gmodel,StationModel station,int type){
		List<StatData> statDatas = new ArrayList<StatData>();
		//获得起止时间
		String bdate = gmodel.getBeginDate();
		String edate = gmodel.getEndDate();
		String groupSql = "";
		//按照统计口径
		String statType = gmodel.getStatType()+"";
		if(statType.equals("1")){//按照每月口径统计
			groupSql = "date_format(collect_time,'%Y-%m')";
		}else if(statType.equals("2")){//按照每周口径统计
			groupSql = "concat(year(collect_time),week(collect_time))";
		}else if(statType.equals("3")){
			groupSql = "date_format(collect_time,'%Y-%m-%d')";
		}
		//通过观测点以及时间得到需要查询的TABLENAME列表
		List<MetadataTable> tableList = tableDao.getTables4Meta(station,bdate, edate, type);
		//拼接SQL语句,获得当前每个统计口径的最高等级参数
		StringBuffer sql = new StringBuffer("");
		//读取配置文件,得到有水质标准的参数信息
		List<IndicatorModel> indicators = new ArrayList<IndicatorModel>();
		StringBuffer standardSql = new StringBuffer("");
		standardSql.append(" select distinct b.id as indicatorid,b.code,b.title");
		standardSql.append(" from sys_domain_threshold a,dm_indicator b,sys_domain_level c");
		standardSql.append(" where a.indicatorcode = b.code");
		standardSql.append(" and b.isactive = 1");
		standardSql.append(" and a.levelid = c.id and c.domainid = ").append(RedDomainId);
		standardSql.append(" order by a.id desc");
		try {
			indicators = this.queryObjectList(standardSql.toString(), IndicatorModel.class);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(indicators==null||indicators.size()<1){
			return null;
		}
		//遍历参数信息,依次查询出在规定范围内的每个统计口径的最高水质标准
		for(IndicatorModel indicator:indicators){
			for(MetadataTable tableName:tableList){
				String tablename = tableName.getTableName();
				StringBuffer standSql = new StringBuffer("");
				standSql.append(" select a.collect_time as xtime,'");
				standSql.append(indicator.getTitle()).append("'  as name,b.code as ydata ");
				standSql.append(" from (");
				standSql.append(" select ").append(groupSql).append(" as collect_time,avg(data) as data");
				standSql.append(" from ").append(tablename);
				standSql.append(" where collect_time between '").append(bdate).append("' and '").append(edate).append("'");
				standSql.append(" and wpid = ").append(station.getId()).append(" and indicator_code = '").append(indicator.getCode()).append("' and collect_type = '1'");
				standSql.append(" and data <> 88888");
				standSql.append(" group by ").append(groupSql).append(") a,sys_domain_level b,sys_domain_threshold c ");
				standSql.append(" where b.domainid = ").append(RedDomainId).append(" and b.id = c.levelid");
				standSql.append(" and c.indicatorcode = '").append(indicator.getCode()).append("'");
				standSql.append(" and case when c.mincal=1 then a.data >= c.min else a.data > c.min end");
				standSql.append(" and case when c.maxcal=1 then a.data <= c.max else a.data < c.max end ");
				standSql.append(" order by ").append(groupSql).append(" ");
				List<StatData> statlist = new ArrayList<StatData>();
				try {
					statlist = this.queryObjectList(standSql.toString(), StatData.class);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i=0;i<statlist.size();i++){
					StatData stat = statlist.get(i);
					//定义是否有匹配 初始化没有匹配
					Boolean ifnothave = true;
					for(StatData statdata:statDatas){
						if(stat.getXtime().equals(statdata.getXtime())){
							if(Integer.parseInt(stat.getYdata())>Integer.parseInt(statdata.getYdata())){
								Collections.replaceAll(statDatas, statdata, stat);
							}
							//
							ifnothave = false;
						}
					}
					//如果没有匹配,则加入数据 并生成正确的顺序
					if(ifnothave){
						//是否有符合条件
						Boolean ifcheck = false;
						//初始化要插入数据的索引
						int index = 0;
						//获得要插入的数据的时间
						String inData = stat.getXtime();
						for(int j=0;j<statDatas.size();j++){
							//获得原来数据的时间
							String stData = statDatas.get(j).getXtime();
							int inst = inData.compareTo(stData);
							if(inst<0&&!ifcheck){
								ifcheck = true;
								index = j;
							}
						}
						if(ifcheck){//如果有匹配的,则根据索引插入数据
							statDatas.add(index,stat);
						}else{ //如果没有匹配的数据,则在最后插入数据
							statDatas.add(stat);
						}
					}
				}
				
			}
		}
		return statDatas;
	}
	
	/*
	 * 为水质统计提供饼形图以及统计列表数据
	 */
	public List<StatData> getData4pie(RedStatisModel model,StationModel station,int type,List<StatData> statDatas){
		List<StatData> list = new ArrayList<StatData>();
		List<StatData> result = new ArrayList<StatData>();
		//获得该统计口径下的总数
		int statCount = 0;
		try {
			statCount = statDatas.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//统计每个水质等级的天数,首要污染因子以及占比
		//初始化每个水质等级
		StringBuffer sql = new StringBuffer("");
		
		sql.append(" select code as standard_grade,name as name,color as color,").append(statCount).append(" as statcount,0 as y,'' as firstThing");
		sql.append(" from sys_domain_level ");
		sql.append(" where domainid = ").append(RedDomainId);
		sql.append(" order by ordercode");
		list = this.queryObjectList(sql.toString(), StatData.class);
		//遍历线性数据的结果,并将结果统计到饼形图中
		for(StatData ad:statDatas){
			for(int i=0;i<list.size();i++){
				StatData li = list.get(i);
				if(Integer.parseInt(ad.getYdata())==li.getStandard_grade()){
					//更新当前水质等级的总数
					li.setY(li.getY()+1);
					//获得首要污染因子
					if(!li.getFirstThing().contains(ad.getName())){
						if(li.getFirstThing().length()>0){
							li.setFirstThing(li.getFirstThing()+",");
						}
						li.setFirstThing(li.getFirstThing()+ad.getName());
					}
				}
				
				list.set(i, li);
			}
		}
		//遍历结果,去掉没有数据的水质等级
		for(StatData li:list){
			if(li.getY()>0){
				result.add(li);
			}
		}
		
		return result;
	}
}

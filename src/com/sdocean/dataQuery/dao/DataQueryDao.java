package com.sdocean.dataQuery.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sdocean.common.model.Result;
import com.sdocean.dataQuery.model.DataChangeModel;
import com.sdocean.dataQuery.model.DataImportModel;
import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.firstpage.model.Ddata;
import com.sdocean.firstpage.model.LastMetaData;
import com.sdocean.firstpage.model.MetaData4FirstPage;
import com.sdocean.frame.dao.OracleEngine;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.dao.IndicatorDao;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataDao;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.HalfHour;
import com.sdocean.metadata.model.MetadataModel;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.metadata.model.SyndataModel;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.dao.StationDao;
import com.sdocean.station.dao.StationDeviceDao;
import com.sdocean.station.model.StationDeviceModel;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;
import com.sdocean.warn.dao.DeviceAlarmDao;
import com.sdocean.warn.dao.RangeDao;
import com.sdocean.warn.model.DeviceAlarmModel;
import com.sdocean.warn.model.RangeModel;

@Component
public class DataQueryDao extends OracleEngine{
	
	@Resource
	MetadataTableDao tableDao;
	@Resource
	IndicatorDao indicatorDao;
	@Resource
	StationDao stationDao;
	@Resource
	MetadataDao metaDataDao;
	@Resource
	RangeDao rangeDao;
	@Resource
	DeviceAlarmDao deviceAlarmDao;
	@Resource
	StationDeviceDao stationDeviceDao;
	
	/*
	 * 为实时数据添加表头
	 */
	public List<UiColumn> getCols4DataQuery(DataQueryModel model){
		List<UiColumn> cols = new ArrayList<UiColumn>();
		StringBuffer sql = new StringBuffer("");
		sql.append(" select '时间' as displayName,'collectTime' as field,'true' as visible,'*' as width,'' as type");
		sql.append(" union all ");
		sql.append(" select * from (");
		sql.append(" select distinct case when b.logo is null or length(b.logo) = 0 then a.title else concat(a.title,'(',b.logo,')') end as displayName,");
		sql.append(" a.code as field,'true' as visible,'*' as width,'number' as type");
		sql.append(" from dm_indicator a left join g_unit b ");
		sql.append(" on a.unitid = b.id ");
		sql.append(" ,view_stationid_deviceid_indicatorid c ");
		sql.append(" where a.id = c.indicatorid");
		sql.append(" and a.isactive = 1");
		sql.append(" and c.deviceid =").append(model.getDeviceId());
		sql.append(" and c.stationid =").append(model.getStationId());
		sql.append(" order by a.ordercode");
		sql.append(") m");
		cols = this.queryObjectList(sql.toString(), UiColumn.class);
		return cols;
	}
	
	/*
	 * 为实时数据查询提供结果
	 */
	public List<Map<String, Object>> getRows4DataQuery(DataQueryModel model){
		List<Map<String, Object>> rows = null;
		Object[] param = new Object[] {
				model.getStationId(),
				model.getDeviceId(),
				model.getBeginDate(),
				model.getEndDate(),
				model.getQueryType()
		};
		String sql = this.handleProcSql("getRows4DataQuery", param);
		rows = this.procForList(sql.toString());
		return rows;
	}
	
	/*
	 * 查询出当前站点信息的最后实时数据
	 * 为首页展示
	 */
	public List<LastMetaData> getData4FirstPage(StationModel station){
		//初始化返回结果
		List<LastMetaData> list = new ArrayList<LastMetaData>();
		//获得要展示的设备以及参数
		StringBuffer devicesql = new StringBuffer("");
		devicesql.append("select distinct b.id as deviceId  ,b.name as deviceName,b.pointNum");
		devicesql.append(" from aiot_firstpage_show a,view_show_station_device b,device_catalog_indicator c,dm_indicator d");
		devicesql.append(" where a.deviceid = b.id and a.wpid = ").append(station.getId());
		devicesql.append(" and b.stationid = ").append(station.getId());
		devicesql.append(" and b.id = c.catalogid and c.indicatorid = d.id and d.isactive = 1");
		devicesql.append(" order by b.ordercode ");
		list = this.queryObjectList(devicesql.toString(), LastMetaData.class);
		//遍历设备列表,得到需要查询的参数列表
		for(LastMetaData device:list){
			StringBuffer indicatorSql = new StringBuffer("");
			indicatorSql.append("select b.id as indicatorId,b.code as indicatorCode,b.title as indicatorTitle,c.logo as unitName,c.description as unitDescription,c.logo as unitLogo ");
			indicatorSql.append(" from aiot_firstpage_show a,dm_indicator b,g_unit c ");
			indicatorSql.append(" where a.indicatorid = b.id and b.isactive = 1 and c.id = b.unitid and a.wpid = ").append(station.getId());
			indicatorSql.append(" and a.deviceid = ").append(device.getDeviceId());
			indicatorSql.append(" order by b.orderCode");
			List<MetaData4FirstPage> metaDatas = new ArrayList<MetaData4FirstPage>();
			metaDatas = this.queryObjectList(indicatorSql.toString(), MetaData4FirstPage.class);
			
			@SuppressWarnings("deprecation")
			Date maxDate = new Date(1, 1, 1, 1, 1, 1);
			
			//遍历metaDatas并得到最新的最大的数据
			for(MetaData4FirstPage metaData:metaDatas){
				//初始化参数,获得当前时间一天前的时间
				StringBuffer dateValue = new StringBuffer("");
				StringBuffer remark = new StringBuffer("");
				DateFormat beginDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				String nowDate = beginDf.format(calendar.getTime());
				Ddata ddata = this.getDataByWDTDI(station, nowDate, 1, device, metaData);
				if(ddata==null){
					calendar.add(Calendar.MONTH, -1);
					nowDate = beginDf.format(calendar.getTime());
					ddata = this.getDataByWDTDI(station, nowDate, 1, device, metaData);
				}
				
				try {
					if(ddata!=null&&ddata.getLastTime()!=null&&beginDf.parse(ddata.getLastTime()).after(maxDate)){
							//获得采集时间
							device.setLastTime(ddata.getLastTime());
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//定义是否显示单位
				Boolean ifunit = true;
				//定义是否执行下一步操作
				Boolean ifcontinue = true;
				//判断数据结果是否为空
				if(ddata!=null&&ddata.getData()!=null){
					//获得数据
					Double mdate = ddata.getData();
					//获得该站点  设备的报警值
					if(ifcontinue) {
						DeviceAlarmModel alarmModel = new DeviceAlarmModel();
						alarmModel.setStationId(station.getId());
						alarmModel.setDeviceId(device.getDeviceId());
						alarmModel.setCollectTime(device.getLastTime());
						List<DeviceAlarmModel> alarmList = deviceAlarmDao.getDeviceAlarmList(alarmModel);
						for(DeviceAlarmModel alarm:alarmList) {
							if(Double.doubleToLongBits(mdate)==Double.doubleToLongBits(alarm.getAlarmData())) {
								dateValue.append(alarm.getConfigName());
								remark.append("(").append(alarm.getConfigName()).append(")");
								ifcontinue=false;
								ifunit = false;
							}
						}
					}
					
					if(ifcontinue) {
						//获得该站点  设备 参数的量程访问
						RangeModel range = rangeDao.getRangeByStationDeviceIndicator(station.getId(), device.getDeviceId(), metaData.getIndicatorCode());
						String dv = "";
						if(range!=null&&range.getId()>0) {
							if(mdate<range.getMindata()) {
								mdate = range.getMindata();
								dv = "<"+String.format("%."+device.getPointNum()+"f", mdate);
							}else if(mdate>range.getMaxdata()) {
								mdate = range.getMaxdata();
								dv = ">"+String.format("%."+device.getPointNum()+"f", mdate);
							}else {
								dv = String.format("%."+device.getPointNum()+"f", mdate);
							}
							
						}else {
							dv = String.format("%."+device.getPointNum()+"f", mdate);
						}
						metaData.setMdata(mdate);
						//设定该设备的有效位数
						dateValue.append(dv);
					}
					//判断该数据是否有效
					if(ddata.getIsactive()==0){
						dateValue.append("(").append("无效数据").append(")");
						remark.append("(").append("无效数据").append(")");
					}
					//判断该数据是否有备注
					if(ddata.getRemark()!=null&&ddata.getRemark().length()>0){
						dateValue.append("[").append(ddata.getRemark()).append("]");
						remark.append("[").append(ddata.getRemark()).append("]");
					}
					/*if(ddata.getMarkCode()!=null&&ddata.getMarkCode().length()>0){
						dateValue.append("[").append(ddata.getMarkCode()).append("]");
					}*/
					
				}else {
					dateValue.append("-");
					ifunit = false;
				}
				if(ifunit) {
					//判断该结果的单位
					if(metaData!=null&&metaData.getUnitLogo()!=null&&metaData.getUnitLogo().length()>0) {
						dateValue.append("(").append(metaData.getUnitLogo()).append(")");
					}
				}
				
				metaData.setDataValue(dateValue.toString());
				metaData.setRemark(remark.toString());
			}
			
			device.setMetaDatas(metaDatas);
		}
		return list;
	}
	
	public Ddata getLastData(String nowDate,StationModel station,DeviceModel device,IndicatorModel indicator) {
		Ddata ddata = null; 
		MetadataTable table = tableDao.getOneTable(station, nowDate, 1);
		String tableName = table.getTableName();
		StringBuffer dsql = new StringBuffer("");
		dsql.append("select collect_time as lastTime, data from ").append(tableName);
		dsql.append(" where wpid = ").append(station.getId()).append(" and indicator_code ='").append(indicator.getCode()).append("'");
		dsql.append(" and data is not null ");
		dsql.append(" and deviceid = ").append(device.getId()); 
		dsql.append(" order by collect_time desc limit 1");
		ddata = this.queryObject(dsql.toString(), Ddata.class);
		return ddata;
	}
	
	//通过站点,起始时间,类型,设备,以及参数查询出实时数据
	public Ddata getDataByWDTDI(StationModel station,String nowDate,int type,LastMetaData device,MetaData4FirstPage indicator){
		Ddata ddata = null; 
		MetadataTable table = tableDao.getOneTable(station, nowDate, type);
		String tableName = table.getTableName();
		StringBuffer dsql = new StringBuffer("");
		dsql.append("select collect_time as lastTime, data,mark_code as markCode,isactive,remark from ").append(tableName);
		dsql.append(" where wpid = ").append(station.getId()).append(" and indicator_code ='").append(indicator.getIndicatorCode()).append("'");
		dsql.append(" and data is not null ");
		dsql.append(" and deviceid = ").append(device.getDeviceId()); 
		dsql.append(" order by collect_time desc limit 1");
		ddata = this.queryObject(dsql.toString(), Ddata.class);
		return ddata;
	}
	/*
	 * 为数据修改的查询提供结果
	 */
	public List<MetadataModel> getResult4DataChangeshow(DataChangeModel model){
		List<MetadataModel> rows = new ArrayList<MetadataModel>();
		//通过站点以及查询的开始以及结束时间,判断要查询的表
		List<MetadataTable> tables = tableDao.getTables4Meta(model.getStation(),model.getBeginDate(), model.getEndDate(), 1);
		StringBuffer sql = new StringBuffer("");  //定义总的SQL语句
		StringBuffer selectSql = new StringBuffer("");
		selectSql.append(" select id,collect_time,data ");
		StringBuffer whereSql = new StringBuffer("");
		whereSql.append(" where wpid = ").append(model.getStation().getId());
		whereSql.append(" and deviceid = ").append(model.getDevice().getId());
		whereSql.append(" and indicator_Code = '").append(model.getIndicator().getCode()).append("'");
		whereSql.append(" and collect_time >= '").append(model.getBeginDate()).append("'");
		whereSql.append(" and collect_time <= '").append(model.getEndDate()).append("'");
		StringBuffer orderSql = new StringBuffer("");
		orderSql.append(" order by collect_time desc");
		for(int i=0;i<tables.size();i++){
			StringBuffer fromSql = new StringBuffer(" from ");
			fromSql.append(tables.get(i).getTableName());
			sql.append(selectSql).append(fromSql).append(whereSql);
			if(i<tables.size()-1){
				sql.append(" union all ");
			}
		}
		sql.append(orderSql);
		rows = this.queryObjectList(sql.toString(), MetadataModel.class);
		return rows;
	}
	/*
	 * 保存修改
	 */
	public Result saveChangeData(DataChangeModel model){
		//初始化返回结果
		Result result = new Result();
		result.setDotype(Result.UPDATE);
		result.setModel(JsonUtil.toJson(model));
		result.setMessage("修改成功");
		result.setResult(Result.SUCCESS);
		//根据站点ID获得站点信息
		StationModel station = stationDao.getStationById(model.getStationId());
		//更改元数据内容
		StringBuffer msql = new StringBuffer("");
		//获得该元数据存放的表
		MetadataTable mtable = tableDao.getOneTable(station, model.getCollect_time(), 1);
		//查询原有数据的具体信息
		 int saveChange =  this.saveChangeDataLog(model);
		if(saveChange>0){
			result.setMessage("保存修改日志失败");
			result.setResult(Result.FAILED);
			return result;
		}
		
		msql.append("insert into ").append(mtable.getTableName()).append(" (collect_time,collect_type,wpid,indicator_code,data,deviceid)");
		msql.append(" values(?,?,?,?,?,?) on duplicate key update data=values(data)");
		Object[] params = new Object[]{
				model.getCollect_time(),1,model.getStationId(),model.getIndicatorCode(),model.getNewData(),model.getDeviceId()
		};
		int mres = 0;
		try {
			mres = this.update(msql.toString(), params);
		} catch (Exception e) {
			// TODO: handle exception
			result.setMessage("修改元数据失败");
			result.setResult(Result.FAILED);
			return result;
		}
		
		//修改综合元数据
		//获得该数据的综合元数据的表
		MetadataTable stable = tableDao.getOneTable(station, model.getCollect_time(), 2);
		//获得该条记录在综合元数据中对应的时间
		HalfHour hh = metaDataDao.getSynDateByMdate(model.getCollect_time());
		//根据时间以及各个参数,查询出在综合元数据表中的记录
		SyndataModel syn = new SyndataModel();
		StringBuffer csql = new StringBuffer("");
		csql.append(" select collect_time,indicator_code as indicatorCode,data,wpid,collect_type,deviceid");
		csql.append(" from ").append(stable.getTableName());
		csql.append("  where collect_time ='").append(hh.getCollectTime()).append("'");
		csql.append(" and wpid = ").append(model.getStationId()).append(" and deviceid = ").append(model.getDeviceId());
		csql.append(" and indicator_code = '").append(model.getIndicatorCode()).append("'");
		try {
			syn = this.queryObject(csql.toString(), SyndataModel.class);
		} catch (Exception e) {
			// TODO: handle exception
			result.setMessage("匹配综合元数据失败");
			result.setResult(Result.FAILED);
			return result;
		}
		if(syn==null||(syn.getData()==model.getOldData())){
			StringBuffer isql = new StringBuffer("");
			isql.append(" insert into ").append(stable.getTableName()).append("(collect_time,indicator_code,data,wpid,collect_type,deviceid)");
			isql.append(" values(?,?,?,?,?,?) on duplicate key update data=values(data)");
			Object[] iparams = new Object[]{
					hh.getCollectTime(),model.getIndicatorCode(),
					model.getNewData(),model.getStationId(),1,model.getDeviceId()
			};
			int sres = 0;
			try {
				sres = this.update(isql.toString(), iparams);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				result.setMessage("更新综合元数据失败");
				result.setResult(Result.FAILED);
			}
		}
		return result;
	}
	
	
	/*
	 * 根据更改数据,保存更改日志
	 */
	public int saveChangeDataLog(DataChangeModel model){
		int result = 0;   //0代表没有错误
		//根据站点ID获得站点信息
		StationModel station = stationDao.getStationById(model.getStationId());		
		//获得该元数据存放的表
		MetadataTable mtable = tableDao.getOneTable(station, model.getCollect_time(), 1);		
		//查询原有数据的具体信息
		StringBuffer oldsql = new StringBuffer("");
		oldsql.append(" select collect_time,collect_type,sensor_type_code,wpid as stationid,indicator_code as indicatorcode,");
		oldsql.append("mark_code,data as olddata,createtime,creator,dataversion,deviceid,unitid");
		oldsql.append(" from ").append(mtable.getTableName());
		oldsql.append(" where collect_time =? and collect_type = ? and wpid = ? and deviceid = ? and indicator_code =  ? limit 1");
		Object[] params = new Object[]{
				model.getCollect_time(),1,model.getStationId(),model.getDeviceId(),model.getIndicatorCode()
		};
		DataChangeModel oldmodel = new DataChangeModel();
		oldmodel = this.queryObject(oldsql.toString(), params,DataChangeModel.class);
		if(oldmodel==null||oldmodel.getStationId()==0){  //没有原来的数据信息, 表明是新增
			oldmodel = new DataChangeModel();
			oldmodel.setCollect_time(model.getCollect_time());
			oldmodel.setCollect_type(1);
			oldmodel.setStationId(model.getStationId());
			oldmodel.setDeviceId(model.getDeviceId());
			oldmodel.setIndicatorCode(model.getIndicatorCode());
			oldmodel.setChangeType(0);  //代表新增
		}else{
			oldmodel.setChangeType(1);  //1代表修改
		}
		oldmodel.setNewData(model.getNewData());
		oldmodel.setUserId(model.getUserId());
		//将数据保存到更新数据库表中
		StringBuffer insertSql = new StringBuffer("");
		insertSql.append(" insert into aiot_metadata_change(collect_time,collect_type,sensor_type_code,wpid,deviceid,indicator_code,mark_code,");
		insertSql.append(" newdata,olddata,createtime,creator,dataversion,changetype,userid)");
		insertSql.append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		Object[] iparams = new Object[]{
				oldmodel.getCollect_time(),oldmodel.getCollect_type(),oldmodel.getSensor_type_code(),oldmodel.getStationId(),oldmodel.getDeviceId(),
				oldmodel.getIndicatorCode(),oldmodel.getMark_code(),oldmodel.getNewData(),oldmodel.getOldData(),oldmodel.getCreateTime(),oldmodel.getCreator(),
				oldmodel.getDataversion(),oldmodel.getChangeType(),oldmodel.getUserId()
		};
		int res = 0;
		try {
			res = this.update(insertSql.toString(), iparams);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			result = 1;
		}
		
		return result;
	}
	
	/*
	 * 将导入数据保存到数据库中
	 */
	public Result saveImportData(List<DataImportModel> list,SysUser user){
		//初始化返回结果
		Result result = new Result();
		//生成版本信息号
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		Date now = new Date();
		String nowString = sdf.format(now);
		String visionData = "T2"+"t"+nowString;
		StringBuffer vision =new StringBuffer("");
		vision.append(" insert into aiot_meta_datavision(code,userid)");
		vision.append(" values('").append(visionData).append("',").append(user.getId()).append(")");
		try {
			this.update(vision.toString(), null);
		} catch (Exception e) {
			// TODO: handle exception
			result.setMessage("保存版本号错误");
			return result;
		}
		
		for(DataImportModel data:list){
			//通过站点ID以及采集时间,确定需要查询的表
			StationModel station = new StationModel();
			station.setId(data.getStationId());
			MetadataTable metaTable = tableDao.getOneTable(station, data.getCollect_time(), 1);
			MetadataTable synTable = tableDao.getOneTable(station, data.getCollect_time(), 2);
			//
		}
		return result;
	}
	
	/*
	 * 得到数据修改日志的数据列表
	 */
	public List<DataChangeModel> getDataChangeLogRows(DataChangeModel model){
		List<DataChangeModel> list = new ArrayList<DataChangeModel>();
		//开始拼接SQL语句
		StringBuffer sql = new StringBuffer("");
		sql.append(" select a.collect_time,a.collect_type,a.sensor_type_code,");
		sql.append(" a.wpid as stationid,b.title as stationName,a.deviceid,c.name as devicename,");
		sql.append(" a.indicator_code as indicatorcode,d.title as indicatorname,a.mark_code,");
		sql.append(" a.newdata,a.olddata,a.createtime,a.creator,a.dataversion,a.userid,e.realname as username,a.changeTime,");
		sql.append(" changetype,case changetype when 0 then '新增' when 1 then '修改' when 2 then '删除' else '' end as changetypename");
		sql.append(" from aiot_metadata_change a,aiot_watch_point b,device_catalog c,");
		sql.append(" dm_indicator d,sys_user e");
		sql.append(" where a.wpid = b.id and a.deviceid = c.id ");
		sql.append(" and a.indicator_code = d.code");
		sql.append(" and a.userid = e.id");
		//添加查询条件
		if(model!=null){
			if(model.getBeginDate()!=null&&model.getBeginDate().length()>0){
				sql.append(" and a.collect_time >= '").append(model.getBeginDate()).append("'");
			}
			if(model.getEndDate()!=null&&model.getEndDate().length()>0){
				sql.append(" and a.collect_time <= '").append(model.getEndDate()).append("'");
			}
			if(model.getStationId()>0){
				sql.append(" and a.wpid = ").append(model.getStationId());
			}
			if(model.getDeviceId()>0){
				sql.append(" and a.deviceid = ").append(model.getDeviceId());
			}
			if(model.getIndicatorId()>0){
				sql.append(" and d.id = ").append(model.getIndicatorId());
			}
		}
		list = this.queryObjectList(sql.toString(), DataChangeModel.class);
		return list;
	}
}

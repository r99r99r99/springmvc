package com.sdocean.dataQuery.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.common.model.Result;
import com.sdocean.dataQuery.dao.DataQueryDao;
import com.sdocean.dataQuery.model.DataChangeModel;
import com.sdocean.dataQuery.model.DataImportModel;
import com.sdocean.dataQuery.model.DataQueryModel;
import com.sdocean.device.model.DeviceModel;
import com.sdocean.firstpage.model.LastMetaData;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.indicator.model.IndicatorModel;
import com.sdocean.metadata.dao.MetadataDao;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataModel;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.page.model.PageResult;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class DataQueryService {
	
	@Autowired
	private DataQueryDao dataQueryDao;
	@Autowired
	private MetadataDao metaDataDao;
	@Autowired
	private MetadataTableDao tableDao;
	/*
	 * 为实时数据添加表头
	 */
	public List<UiColumn> getCols4DataQuery(DataQueryModel model){
		return dataQueryDao.getCols4DataQuery(model);
	}
	
	/*
	 * 为实时数据查询提供结果
	 */
	public List<Map<String, Object>> getRows4DataQuery(DataQueryModel model){
		return dataQueryDao.getRows4DataQuery(model);
	}
	
	/*
	 * 查询出当前站点信息的最后实时数据
	 * 为首页展示
	 */
	public List<LastMetaData> getData4FirstPage(StationModel station){
		return dataQueryDao.getData4FirstPage(station);
	}
	
	/*
	 * 为数据修改查询结果提供表头
	 */
	public List<UiColumn> getCols4DataChange(DataChangeModel model){
		List<UiColumn> cols = new ArrayList<UiColumn>();
		UiColumn col1 = new UiColumn("id", "id", false, "*", false, "number");
		UiColumn col2 = new UiColumn("时间", "collect_time", true, "*", true, "");
		//col2.setCellFilter("data:\"yyyy-MM-dd HH:mm:ss.0\"");
		UiColumn col3 = new UiColumn("数值", "data", true, "*", true, "number");
		cols.add(col1);
		cols.add(col2);
		cols.add(col3);
		return cols;
	}
	/*
	 * 为数据修改的查询提供结果
	 */
	public List<MetadataModel> getResult4DataChangeshow(DataChangeModel model){
		return dataQueryDao.getResult4DataChangeshow(model);
	}
	/*
	 * 保存修改
	 */
	public Result saveChangeData(DataChangeModel model){
		return dataQueryDao.saveChangeData(model);
	}
	/*
	 * 保存数据导入
	 */
	public Result saveImportData(DataImportModel model,SysUser user){
		Result result = new Result();
		result.setDotype(Result.ADD);
		result.setModel(JsonUtil.toJson(model));
		result.setResult(Result.FAILED);
		result.setMessage("导入数据成功");
		List<DataImportModel> list = new ArrayList<DataImportModel>();
		try {
			list = (List<DataImportModel>) JsonUtil.fromJsons(model.getImportString(), DataImportModel.class);
		} catch (Exception e) {
			result.setResult(Result.FAILED);
			result.setMessage("数据错误");
			return result;
		}
		if(list==null||list.size()<1){
			result.setResult(Result.FAILED);
			result.setMessage("数据错误");
			return result;
		}
		
		//将其他形式的元数据转换成标准元数据
		List<MetadataModel> metas = new ArrayList<MetadataModel>();
		metas = metaDataDao.changeMetadata(list, 1, "csvImport");
		//整合数据采集时间
		for(MetadataModel meta:metas){
			meta = metaDataDao.changeNewMetadata(meta);
			StationModel station = new StationModel();
			station.setId(meta.getWpId());
			//将标准元数据存入到基础元数据表中
			MetadataTable metaTable = tableDao.getOneTable(station, meta.getCollect_time(), 1);
			
			DataChangeModel dmodel = new DataChangeModel();
			dmodel.setCollect_time(meta.getCollect_time());
			dmodel.setStationId(meta.getWpId());
			dmodel.setDeviceId(meta.getDeviceId());
			dmodel.setIndicatorCode(meta.getIndicator_code());
			dmodel.setNewData(meta.getData());
			dmodel.setUserId(user.getId());
			dataQueryDao.saveChangeDataLog(dmodel);
			try {
				metaDataDao.saveMetaData(metaTable.getTableName(), meta);
			} catch (Exception e) {
				// TODO: handle exception
				result.setResult(Result.FAILED);
				return result;
			}
			//将标准元数据存入到综合元数据表中
			MetadataTable  synTable = tableDao.getOneTable(station, meta.getCollect_time(), 2);
			try {
				metaDataDao.saveSynData(synTable.getTableName(), meta);
			} catch (Exception e) {
				// TODO: handle exception
				result.setResult(Result.FAILED);
				result.setMessage("导入综合元数据表错误");
				return result;
			}
		}
		return result;
	}
	
	/*
	 * 为数据修改日志查询提供表头
	 * 
	 */
	public List<UiColumn> getDataChangeLogCols(DataChangeModel model){
		List<UiColumn> cols = new ArrayList<UiColumn>();
		UiColumn col1 = new UiColumn("id", "id", false, "*", false, "number");
		UiColumn col2 = new UiColumn("时间", "collect_time", true, "*", true, "");
		UiColumn col3 = new UiColumn("wpid", "stationId", false, "*", false, "");
		UiColumn col4 = new UiColumn("站点", "stationName", true, "*", false, "");
		UiColumn col5 = new UiColumn("deviceId", "deviceId", false, "*", false, "");
		UiColumn col6 = new UiColumn("设备", "deviceName", true, "*", false, "");
		UiColumn col7 = new UiColumn("indicatorCode", "indicatorCode", false, "*", false, "");
		UiColumn col8 = new UiColumn("参数", "indicatorName", true, "*", false, "");
		UiColumn col9 = new UiColumn("原数据", "oldData", true, "*", false, "");
		UiColumn col10 = new UiColumn("新数据", "newData", true, "*", false, "");
		UiColumn col11 = new UiColumn("changeType", "changeType", false, "*", false, "");
		UiColumn col12 = new UiColumn("变更类型", "changeTypeName", true, "*", false, "");
		UiColumn col13 = new UiColumn("userId", "userId", false, "*", false, "");
		UiColumn col14 = new UiColumn("操作人员", "userName", true, "*", false, "");
		UiColumn col15 = new UiColumn("修改时间", "changeTime", true, "*", false, "");
		
		
		cols.add(col1);
		cols.add(col2);
		cols.add(col3);
		cols.add(col4);
		cols.add(col5);
		cols.add(col6);
		cols.add(col7);
		cols.add(col8);
		cols.add(col9);
		cols.add(col10);
		cols.add(col11);
		cols.add(col12);
		cols.add(col13);
		cols.add(col14);
		cols.add(col15);
		return cols;
	}
	
	/*
	 * 得到数据修改日志的数据列表
	 */
	public List<DataChangeModel> getDataChangeLogRows(DataChangeModel model){
		return dataQueryDao.getDataChangeLogRows(model);
	}
}

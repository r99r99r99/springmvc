package com.sdocean.notice.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sdocean.common.model.Result;
import com.sdocean.frame.util.JsonUtil;
import com.sdocean.log.dao.OperationLogDao;
import com.sdocean.log.model.OperationLogModel;
import com.sdocean.menu.model.CurrMenu;
import com.sdocean.metadata.dao.MetadataTableDao;
import com.sdocean.metadata.model.MetadataTable;
import com.sdocean.notice.dao.NoticeDao;
import com.sdocean.notice.model.NoticeModel;
import com.sdocean.page.model.UiColumn;
import com.sdocean.station.model.StationModel;
import com.sdocean.users.model.SysUser;

@Service
@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRED)
public class NoticeService {
	
	@Autowired
	NoticeDao noticeDao;
	
	/*
	 * 查询通知列表的表头
	 */
	public List<UiColumn> getCols4NoticeList(){
		List<UiColumn> list = new ArrayList<>();
		UiColumn col1 = new UiColumn("id", "id", false, "*");
		UiColumn col2 = new UiColumn("stationId", "stationId", false, "*");
		UiColumn col3 = new UiColumn("站点", "stationName", true, "*");
		UiColumn col4 = new UiColumn("标题", "title", true, "*");
		UiColumn col5 = new UiColumn("内容", "text", false, "*");
		UiColumn col6 = new UiColumn("userId", "userId", false, "*");
		UiColumn col7 = new UiColumn("上报/下发人", "userName", true, "*");
		UiColumn col8 = new UiColumn("上报/下发时间", "collectTime", true, "*");
		UiColumn col9 = new UiColumn("类型", "typeName", true, "*");
		UiColumn col10 = new UiColumn("type", "type", false, "*");
		list.add(col1);
		list.add(col2);
		list.add(col3);
		list.add(col4);
		list.add(col5);
		list.add(col6);
		list.add(col7);
		list.add(col8);
		list.add(col9);
		list.add(col10);
		return list;
	}
	/*
	 * 查询通知列表
	 */
	public List<NoticeModel> getNoticeList(NoticeModel model){
		return noticeDao.getNoticeList(model);
	}
	/*
	 * 根据ID查询单个通知
	 */
	public NoticeModel getNoticeById(int id){
		return noticeDao.getNoticeById(id);
	}
	/*
	 * 通知上报
	 * 一个站点上报给服务中心
	 */
	public Result saveNoticeUp(NoticeModel model){
		return noticeDao.saveNoticeUp(model);
	}
	/*
	 * 通知下发
	 */
	public Result saveNoticeDown(NoticeModel model){
		return noticeDao.saveNoticeDown(model);
	}
	/*
	 * 删除通知
	 * 只能删除当前人生成的通知
	 * 只能再生成通知后的1小时内删除
	 */
	public Result deleNotice(NoticeModel model){
		return noticeDao.deleNotice(model);
	}

	/*
	 * 读取该人员未读通知的列表
	 */
	public List<NoticeModel> getNoReadNoticeListByUser(NoticeModel model,SysUser user){
		return noticeDao.getNoReadNoticeListByUser(model, user);
	}
	/*
	 * 将该用户的所有未读信息设置为已读
	 */
	public void saveReadNoticeByUser(SysUser user){
		 noticeDao.saveReadNoticeByUser(user);
	}
	/*
	 * 获取该人员第一条未读通知的记录
	 */
	public NoticeModel getLastNoReadNoticeListByUser(SysUser user){
		return noticeDao.getLastNoReadNoticeListByUser(user);
	}
}

package com.sdocean.file.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.sdocean.frame.model.ConfigInfo;
import com.sdocean.frame.util.JsonUtil;

public class FileUpload {
	
	/*
	 * 根据路径,把文件存入到路径下,
	 * 并返回新生成的文件名称
	 */
	public String saveFileUpload(ConfigInfo info,String filePath,MultipartFile file) throws Exception{
		int pre = (int) System.currentTimeMillis();
		FileOutputStream os = null;
		FileInputStream in = null;
		//定义新文件名称
		String fileName = new Date().getTime() + file.getOriginalFilename();
		
		//try {
			os = new FileOutputStream(info.getSysPath()+"/"+ filePath +"/"+ fileName);
			in = (FileInputStream) file.getInputStream();
			 //以写字节的方式写文件  
            int b = 0;  
            while((b=in.read()) != -1){  
                os.write(b);  
            }  
            os.flush();  
            
            int finaltime = (int) System.currentTimeMillis();  
		//} catch (Exception  e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}finally{
			try {
				os.close();
				in.close(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		//}
		
		return fileName;
	}
	
	/*
	 * 根据路径,吧文件存入路径下,
	 * 并返回新生成的文件名称
	 */
	public String saveFile(ConfigInfo info,String filePath,MultipartFile file){
		String fpath = info.getSysPath()+"/"+filePath;
		File fpFile = new File(fpath);
		if(!fpFile.exists()) {
			fpFile.mkdirs();
		}
		//定义新文件名称
		String fileName = new Date().getTime() + file.getOriginalFilename();
         //定义上传路径  
           String path = info.getSysPath()+"/"+filePath+"/" + fileName;  
           File localFile = new File(path);  
           try {
				file.transferTo(localFile);
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
           
        fileName =   filePath+"/" + fileName;
		return fileName;
	}
	/*
	 * 保存站点维护记录
	 */
	public String saveMainFile(ConfigInfo info,String filePath,MultipartFile file){
		//定义新文件名称
		String fileName = new Date().getTime() + file.getOriginalFilename();
		String fpath = info.getMainFilePath()+"/"+filePath;
		File fpFile = new File(fpath);
		if(!fpFile.exists()) {
			fpFile.mkdirs();
		}
         //定义上传路径  
           String path = info.getMainFilePath()+"/"+filePath+"/" + fileName;  
           File localFile = new File(path);  
           try {
				file.transferTo(localFile);
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
           
        fileName =   filePath+"/" + fileName;
		return fileName;
	}
	
	/*
	 * 下载文件
	 */
	public void downFile(String fileName,String filePath,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		//String name = "graphquery_init.js";
		//String path = "E:\\mainFile\\mfile\\"+"1521684025366graphquery_init.js";
		//声明本次下载状态的记录对象
	    //设置响应头和客户端保存文件名
	    response.setCharacterEncoding("utf-8");
	    response.setContentType("multipart/form-data");
	    response.setHeader("Content-Disposition", "attachment;fileName=" + new String(fileName.getBytes("GBK"), "ISO8859-1"));
	    //用于记录以完成的下载的数据量，单位是byte
	    long downloadedLength = 0l;
	    try {
	        //打开本地文件流
	        InputStream inputStream = new FileInputStream(filePath);
	        //激活下载操作
	        OutputStream os = response.getOutputStream();

	        //循环写入输出流
	        byte[] b = new byte[2048];
	        int length;
	        while ((length = inputStream.read(b)) > 0) {
	            os.write(b, 0, length);
	            downloadedLength += b.length;
	        }

	        // 这里主要关闭。
	        os.close();
	        inputStream.close();
	    } catch (Exception e){
	        throw e;
	    }
	}
}	

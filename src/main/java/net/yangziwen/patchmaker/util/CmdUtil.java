package net.yangziwen.patchmaker.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CmdUtil {
	
	private CmdUtil(){}
	
	public static <T> Map<String, T> consume(Process process, CmdCallback<T> callback) {
		if(process == null) {
			return Collections.emptyMap();
		}
		Map<String, T> resultMap = new LinkedHashMap<>();	// 保持顺序
		String line = "";
		try (
			InputStream in = process.getInputStream(); 
			BufferedReader reader =  new BufferedReader(new InputStreamReader(in, "UTF-8"));
		) {
			while((line = reader.readLine()) != null) {
				callback.process(line, resultMap);
			}
			callback.afterProcess(resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return resultMap;
	}
	
	public static int waitForProcess(Process process) {
		if(process != null) {
			try {
				return process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static abstract class CmdCallback<T> {

		protected abstract void process(String line, Map<String, T> resultMap);
		
		protected void afterProcess(Map<String, T> resultMap) {
			// defaultly do nothing
		}
		
	}
	
}

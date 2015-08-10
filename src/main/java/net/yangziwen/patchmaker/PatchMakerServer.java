package net.yangziwen.patchmaker;

import org.apache.commons.lang3.math.NumberUtils;

import net.yangziwen.patchmaker.controller.FileController;
import net.yangziwen.patchmaker.controller.GitController;
import net.yangziwen.patchmaker.controller.PatchController;
import spark.Spark;

public class PatchMakerServer {
	
	public static void main(String[] args) {
		
		int port = NumberUtils.toInt(System.getProperty("port"), 8082);
		
		Spark.port(port);
		
		Spark.ipAddress("127.0.0.1");
		
		Spark.staticFileLocation("/static");
		
		FileController.init();
		
		GitController.init();
		
		PatchController.init();
	}
	
}

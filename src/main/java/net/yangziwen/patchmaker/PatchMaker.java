package net.yangziwen.patchmaker;

import net.yangziwen.patchmaker.controller.FileController;
import net.yangziwen.patchmaker.controller.GitController;
import spark.Spark;

public class PatchMaker {
	public static void main(String[] args) {
		
		Spark.port(8082);
		
		Spark.staticFileLocation("/static");
		
		FileController.init();
		
		GitController.init();
	}
}

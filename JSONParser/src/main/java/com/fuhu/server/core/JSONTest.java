package com.fuhu.server.core;

import java.io.File;
import java.io.IOException;

public class JSONTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		JSONPaser paser = new JSONPaser();
		// paser.autoParsing("C:\\Users\\william.lan\\Desktop\\test3Order3User\\00be4753-10ee-4caa-a29d-dd583f470c1d.json",
		// "C:\\Users\\william.lan\\Desktop\\","id",
		// "C:\\Users\\william.lan\\Desktop\\00be4753-10ee-4caa-a29d-dd583f470c1d_1.json");
		// paser.autoParsing(
		// "C:\\Users\\william.lan\\Desktop\\nabi2-all-fields.json",
		// "C:\\Users\\william.lan\\Desktop\\",
		// "productsList->productImageList->link",
		// "C:\\Users\\william.lan\\Desktop\\nabi2-all-fields_1.json");
//		paser.autoParsingByPath(
//				"C:\\Users\\william.lan\\Git\\nabi\\files\\nabipass\\categories\\nabi2\\",
//				"categoriesList->categoryDescription", "C:\\Users\\william.lan\\Desktop\\11111.json");
		// paser.test("C:\\Users\\william.lan\\Desktop\\test.json",
		// "C:\\Users\\william.lan\\Desktop\\", "id", "mediaList", 2);
		paser.test2("C:\\Users\\william.lan\\Desktop\\2\\2\\0\\dreamtab\\0", "C:\\Users\\william.lan\\Git\\nabi\\files\\nabipass\\products\\dreamtab");

	}

}

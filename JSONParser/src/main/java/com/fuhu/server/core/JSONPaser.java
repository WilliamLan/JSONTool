package com.fuhu.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONPaser {

	final int OBJECT = 1;
	final int ARRAY = 2;
	final int FIELD = 3;
	JSONArray jsonList;
	JSONObject jsonObj;

	public JSONPaser() {
		super();
		jsonList = new JSONArray();
		jsonObj = new JSONObject();
	}

	/**
	 * parse to JSON object from json file
	 * 
	 * @param path
	 *            - json file full-path
	 * @return JSONObject
	 */
	public JSONObject parseToJSONObject(String path) {
		JSONObject jsonObject = null;
		try {

			// read the json file
			FileReader reader = new FileReader(path);

			JSONParser jsonParser = new JSONParser();
			// parse JSON object from json file(*.json)
			jsonObject = (JSONObject) jsonParser.parse(reader);

		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return jsonObject;

	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void autoParsing(String sourcePath, String comparePath,
			String source, String destPath) {

		// get JSON file from source path
		JSONObject sourceObj = parseToJSONObject(sourcePath);
		Map<String, JSONObject> hash = new HashMap<String, JSONObject>();
		boolean arrayObjFlag = false;
		boolean objFlag = false, arrayFlag = false;
		//
		String index = null;
		String[] array = source.split("->");
		if (array.length == 0) {
			array = new String[1];
			array[0] = source;
		}
		for (int i = 0; i < array.length; i++) {
			JSONObject innerObj = null;
			JSONArray innerArray = null;
			String property = null;
			long intproperty = 0;
			System.out.println("The " + (i + 1) + " Query String---->> "
					+ array[i]);
			System.out.println("Content--->>"
					+ sourceObj.get(array[i]).toString());
			if (sourceObj.get(array[i]) != null) {
				arrayObjFlag = sourceObj.get(array[i]).toString()
						.startsWith("[{");
			}

			if (!arrayObjFlag) {
				if (sourceObj.get(array[i]) != null) {
					objFlag = sourceObj.get(array[i]).toString()
							.startsWith("{");
				}
				if (!objFlag) {
					if (sourceObj.get(array[i]) != null) {
						arrayFlag = sourceObj.get(array[i]).toString()
								.startsWith("[");
					}
				}
			}

			// check object of JsonArray
			if (arrayObjFlag) {
				innerArray = (JSONArray) sourceObj.get(array[i]);
				System.out.println("ArrayObj~~~>>" + innerArray);
				innerObj = new JSONObject();
				//
				if (i == array.length - 1) {
					JSONArray jsonarray = new JSONArray();
					for (int k = 0; k < innerArray.size(); k++) {
						JSONObject o = (JSONObject) innerArray.get(k);
						System.out.println("~~~>>" + o.get(array[i]));
						// Get information with given string
						jsonarray.add(o.get(array[i]));
					}
					innerObj.put(array[i], jsonarray);
					hash.put(array[i], innerObj);
					System.out
							.println("-------------------ArrayObj end------------------");
					break;
				} else {
					JSONArray jsonarray = new JSONArray();
					for (int k = 0; k < innerArray.size(); k++) {
						JSONObject o = (JSONObject) innerArray.get(k);
						System.out.println("~~~>>" + o.get(array[i + 1]));
						// Get information with given string
						jsonarray.add(o.get(array[i + 1]));
					}

					innerObj.put(array[i + 1], jsonarray);
					hash.put(array[i + 1], innerObj);
					index = array[i + 1];
					sourceObj = innerObj;
				}
			} else
			// check JsonObject
			if (objFlag) {
				innerObj = (JSONObject) sourceObj.get(array[i]);
				System.out.println("Obj~~~>>" + innerObj);
				//
				if (i == array.length - 1) {
					index = array[i];
					JSONObject inner = new JSONObject();
					inner.put(array[i], innerObj);
					hash.put(array[i], inner);
					System.out
							.println("-------------------Obj end------------------");
					break;
				} else {
					hash.put(array[i], innerObj);
					// } else {
					// hash.get(array[i - 1]).put(array[i], innerObj);
					index = array[i];
					// }
					sourceObj = innerObj;
				}
			} else
			// check JsonArray
			if (arrayFlag) {
				innerArray = (JSONArray) sourceObj.get(array[i]);
				System.out.println("Array~~~>>" + innerArray);
				//
				innerObj = new JSONObject();
				//
				if (i == array.length - 1) {
					index = array[i];
					JSONObject inner = new JSONObject();
					inner.put(array[i], innerArray);
					hash.put(array[i], inner);
					System.out
							.println("-------------------Array end------------------");
					break;
				} else {
					JSONArray jsonarray = new JSONArray();
					for (int k = 0; k < innerArray.size(); k++) {
						JSONArray o = (JSONArray) innerArray.get(k);
						System.out.println("~~~>>" + (JSONObject) o.get(0));
						// Get information with given string
						jsonarray.add((JSONObject) o.get(0));
					}
					innerObj.put(array[i + 1], jsonarray);
					hash.put(array[i + 1], innerObj);
					index = array[i + 1];
					sourceObj = innerObj;
				}
			} else {
				innerObj = new JSONObject();
				try {
					property = (String) sourceObj.get(array[i]);
					innerObj.put(array[i], property);
					System.out.println("Property~~~>>" + property);
				} catch (Exception e) {
					try {
						intproperty = (Long) sourceObj.get(array[i]);
						innerObj.put(array[i], intproperty);
						System.out.println("Property~~~>>" + intproperty);
					} catch (Exception ez) {
						e.printStackTrace();
					}
				}

				hash.put(array[i], innerObj);

				index = array[i];
			}

		}

		// write to given file
		try {
			FileWriter writer = new FileWriter(destPath);
			// sourceObj.put("products", new_products);
			writer.write(hash.get(index).toJSONString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @throws IOException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void autoParsingByPath(String sourcePath, String source,
			String destPath) throws IOException {
		File folder = new File(sourcePath);
		boolean arrayObjFlag = false;
		boolean objFlag = false, arrayFlag = false;
		// Check file is existed or not
		if (folder.exists()) {
			//
			Map<String, JSONObject> hash = new HashMap<String, JSONObject>();
			//
			if (folder.isDirectory()) {
				for (File file : folder.listFiles()) {
					autoParsingByPath(file.getCanonicalPath(), source, destPath);
				}
			} else {
				// for (File file : folder.listFiles()) {
				// get JSON file from source path
				JSONObject sourceObj = parseToJSONObject(folder
						.getCanonicalPath());
				System.out.println("File~~~~>>" + folder.getCanonicalPath());
				//
				String index = null;
				String[] array = source.split("->");
				if (array.length == 0) {
					array = new String[1];
					array[0] = source;
				}
				for (int i = 0; i < array.length; i++) {
					JSONObject innerObj = null;
					JSONArray innerArray = null;
					String property = null;
					long intproperty = 0;
					System.out.println("The " + (i + 1)
							+ " Query String---->> " + array[i]);
					if (sourceObj.get(array[i]) != null) {
						System.out.println("Content--->>"
								+ sourceObj.get(array[i]).toString());
					} else {
						System.out.println("Content--->> Null");
					}
					if (sourceObj.get(array[i]) != null) {
						arrayObjFlag = sourceObj.get(array[i]).toString()
								.startsWith("[{");
					}

					if (!arrayObjFlag) {
						if (sourceObj.get(array[i]) != null) {
							objFlag = sourceObj.get(array[i]).toString()
									.startsWith("{");
						}
						if (!objFlag) {
							if (sourceObj.get(array[i]) != null) {
								arrayFlag = sourceObj.get(array[i]).toString()
										.startsWith("[");
							}
						}
					}

					// check object of JsonArray
					if (arrayObjFlag) {
						innerArray = (JSONArray) sourceObj.get(array[i]);
						System.out.println("ArrayObj~~~>>" + innerArray);
						innerObj = new JSONObject();
						//
						if (i == array.length - 1) {
							JSONArray jsonarray = new JSONArray();
							for (int k = 0; k < innerArray.size(); k++) {
								JSONObject o = (JSONObject) innerArray.get(k);
								System.out.println("~~~>>" + o.get(array[i]));
								// Get information with given string
								jsonarray.add(o.get(array[i]));
							}
							innerObj.put(array[i], jsonarray);
							hash.put(array[i], innerObj);
							System.out
									.println("-------------------ArrayObj end------------------");
							break;
						} else {
							JSONArray jsonarray = new JSONArray();
							for (int k = 0; k < innerArray.size(); k++) {
								JSONObject o = (JSONObject) innerArray.get(k);
								System.out.println("~~~>>"
										+ o.get(array[i + 1]));
								// Get information with given string
								jsonarray.add(o.get(array[i + 1]));
							}

							innerObj.put(array[i + 1], jsonarray);
							hash.put(array[i + 1], innerObj);
							index = array[i + 1];
							sourceObj = innerObj;
						}
					} else
					// check JsonObject
					if (objFlag) {
						innerObj = (JSONObject) sourceObj.get(array[i]);
						System.out.println("Obj~~~>>" + innerObj);
						//
						if (i == array.length - 1) {
							index = array[i];
							JSONObject inner = new JSONObject();
							inner.put(array[i], innerObj);
							hash.put(array[i], inner);
							System.out
									.println("-------------------Obj end------------------");
							break;
						} else {
							hash.put(array[i], innerObj);
							// } else {
							// hash.get(array[i - 1]).put(array[i], innerObj);
							index = array[i];
							// }
							sourceObj = innerObj;
						}
					} else
					// check JsonArray
					if (arrayFlag) {
						innerArray = (JSONArray) sourceObj.get(array[i]);
						System.out.println("Array~~~>>" + innerArray);
						//
						innerObj = new JSONObject();
						//
						if (i == array.length - 1) {
							index = array[i];
							JSONObject inner = new JSONObject();
							inner.put(array[i], innerArray);
							hash.put(array[i], inner);
							System.out
									.println("-------------------Array end------------------");
							break;
						} else {
							JSONArray jsonarray = new JSONArray();
							for (int k = 0; k < innerArray.size(); k++) {
								JSONArray o = (JSONArray) innerArray.get(k);
								System.out.println("~~~>>"
										+ (JSONObject) o.get(0));
								// Get information with given string
								jsonarray.add((JSONObject) o.get(0));
							}
							innerObj.put(array[i + 1], jsonarray);
							hash.put(array[i + 1], innerObj);
							index = array[i + 1];
							sourceObj = innerObj;
						}
					} else {
						innerObj = new JSONObject();
						try {
							property = (String) sourceObj.get(array[i]);
							innerObj.put(array[i], property);
							System.out.println("Property~~~>>" + property);
						} catch (Exception e) {
							try {
								intproperty = (Long) sourceObj.get(array[i]);
								innerObj.put(array[i], intproperty);
								System.out.println("Property~~~>>"
										+ intproperty);
							} catch (Exception ez) {
								e.printStackTrace();
							}
						}

						hash.put(array[i], innerObj);

						index = array[i];
					}
					jsonList.add(innerObj);
				}
				jsonObj.put("Result", jsonList);
				// write to given file
				try {
					FileWriter writer = new FileWriter(destPath);

					writer.write(jsonObj.toJSONString());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		// }
	}

	// public void showAll(File file) throws IOException {
	//
	// if (file.isDirectory()) {
	// for (File f : file.listFiles()) {
	// showAll(f);
	// }
	// } else {
	// System.out.println("~~~~>>>" + file.getCanonicalPath());
	// }
	//
	// }

	public void test(String sourcePath, String comparePath, String origin,
			String target, int type) {
		// get JSON file from source path
		JSONObject sourceObj = parseToJSONObject(sourcePath);
		// get compared path
		File compareFile = new File(comparePath);
		JSONObject compareObj = null;
		if (compareFile.exists()) {
			//
			for (File file : compareFile.listFiles()) {

				// search by productId
				if (file.getName().equals(sourceObj.get(origin) + ".json")) {

					try {
						compareObj = parseToJSONObject(file.getCanonicalPath());
						//
						JSONArray array = null;
						JSONObject obj = null;
						String goal = null;
						Integer num = null;
						switch (type) {

						// Type is Object
						case 1:
							obj = (JSONObject) sourceObj.get(target);
							compareObj.put(target, obj);
							break;
						// Type is Array
						case 2:
							// get product array from JSON Object
							array = (JSONArray) sourceObj.get(target);
							compareObj.put(target, array);
							break;
						// Type is String
						case 3:
							goal = (String) sourceObj.get(target);
							compareObj.put(target, goal);
							break;
						// Type is Interger
						case 4:
							num = (Integer) sourceObj.get(target);
							compareObj.put(target, num);
							break;
						//
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			// write to given file
			try {
				FileWriter writer = new FileWriter(
						"C:\\Users\\william.lan\\Desktop\\111.json");
				writer.write(compareObj.toJSONString());
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * @throws IOException
	 * 
	 */
	public void test2(String sourcePath, String comparePath) throws IOException {

		File source = new File(sourcePath);
		File compare = new File(comparePath);
		String id = null;
		int studioCount = 0;
		int developerNameCount = 0;

		if (source.exists()) {
			if (source.isDirectory()) {
				for (File f : source.listFiles()) {
					test2(f.getCanonicalPath(), comparePath);
				}
			} else {
				// get JSON file from source path
				JSONObject sourceObj = parseToJSONObject(source
						.getCanonicalPath());
				JSONArray array = (JSONArray) sourceObj.get("products");
				// Setting output file name
				id = source.getName().substring(0,
						source.getName().indexOf("."));
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = (JSONObject) array.get(i);
					String str = (String) obj.get("productId");
                    // remove unwanted data 
					JSONArray tempList = new JSONArray();
					JSONArray mediaList = (JSONArray) obj.get("mediaList");
					for (int j = 0; j < mediaList.size(); j++) {
						JSONObject media = (JSONObject) mediaList.get(j);
						String imageName = (String) media.get("imageName");
						if (imageName != null) {
							if (!imageName.equals("icon_bottom_1")) {
								tempList.add(media);
							}
						}
					}
					obj.put("mediaList", tempList);
					//
					if (str != null) {
						for (File comp : compare.listFiles()) {
							String str2 = comp.getName().substring(0,
									comp.getName().indexOf("."));
							if (str.equals(str2)) {
								JSONObject inner = parseToJSONObject(comp
										.getCanonicalPath());
								JSONObject o = (JSONObject) inner
										.get("information");
								String studio = (String) o.get("studio");
								String developerName = (String) o
										.get("developerName");
								// check studio
								if (studio != null) {
									System.out.println("studio-->>" + studio);
									obj.put("studio", studio);
									studioCount++;
								}
								// check developerName
								if (developerName != null) {
									System.out.println("developerName-->>"
											+ developerName);
									obj.put("developerName", developerName);
									developerNameCount++;
								}
							}
						}
					}
					array.set(i, obj);
				}
				sourceObj.put("products", array);

				// write to given file
				try {
					FileWriter writer = new FileWriter(
							"C:\\Users\\william.lan\\Desktop\\test\\" + id
									+ ".json");
					writer.write(sourceObj.toJSONString());
					writer.flush();
					writer.close();
					System.out
							.println("studio mapping Count-->>" + studioCount);
					System.out.println("developerName mapping Count-->>"
							+ developerNameCount);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}
}

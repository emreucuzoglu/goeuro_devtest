package de.goeuro.devtest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author emre.ucuzoglu
 */
public class GoEuroTest {
   private static final Logger LOGGER = Logger.getLogger(GoEuroTest.class.getName());

   private static final int EXPECTED_NUMBER_OF_ARGS = 1;
   private static final String URL_CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();
   private static final StringBuilder URL_MAIN = new StringBuilder("http://api.goeuro.com/api/v2/position/suggest/en/");

   private static final String KEY_INNER = "inner";
   private static final String KEY_VALUE = "value";
   private static final String NEW_LINE = "\n";
   private static final String SEPARATOR = ",";

   private static final String FILE_NAME = "./result.csv";

   private static final Object[] RESULT_FIELDS = new Object[] { "_id", "name", "type",
         new JSONObject("{" + KEY_INNER + ":geo_position, " + KEY_VALUE + ":latitude}"),
         new JSONObject("{" + KEY_INNER + ":geo_position, " + KEY_VALUE + ":longitude}") };

   public static void main(String[] args) {

      if (args.length != EXPECTED_NUMBER_OF_ARGS) {
         throw new IllegalArgumentException(String.format("Wrong number of arguments. Expected %d got %d ", EXPECTED_NUMBER_OF_ARGS, args.length));
      }

      try {
         JSONArray jsonInputArray = GoEuroTest.getJsonFromAPI(args[0]);
         writeToFile(FILE_NAME, createCSVData(jsonInputArray));
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
      }
   }

   /**
    * @param fileName
    * @param data
    * @throws IOException
    */
   private static void writeToFile(String fileName, String data) throws IOException {
      FileWriter writer;
      try {
         writer = new FileWriter(fileName);
         writer.append(data);
         writer.flush();
         writer.close();
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         throw new IOException("Error writing file : " + fileName);
      }

   }

   /**
    * creates data for CVS file
    * 
    * @param jsonInputArray
    * @return
    */
   private static String createCSVData(JSONArray jsonInputArray) {
      StringBuilder stringBuilder = new StringBuilder(writeHeaderString());

      for (int i = 0; i < jsonInputArray.length(); i++) {
         stringBuilder.append(getFields(jsonInputArray.optJSONObject(i)));
      }

      return stringBuilder.toString();
   }

   /**
    * returns static head line for CVS declared in array RESULT_FIELDS
    * 
    * @return
    */
   private static String writeHeaderString() {

      StringBuilder stringBuilder = new StringBuilder();

      for (int i = 0; i < RESULT_FIELDS.length; i++) {
         if (RESULT_FIELDS[i] instanceof JSONObject) {
            stringBuilder.append(((JSONObject) RESULT_FIELDS[i]).optString(KEY_INNER));
            stringBuilder.append(SEPARATOR);
         } else {
            stringBuilder.append(RESULT_FIELDS[i]);
            stringBuilder.append(SEPARATOR);
         }
      }
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      stringBuilder.append(NEW_LINE);

      return stringBuilder.toString();

   }

   /**
    * returns inputObject's fields declared in array RESULT_FIELDS
    * 
    * @param inputObject
    * @return JSONObject
    */
   private static String getFields(JSONObject inputObject) {
      StringBuilder stringBuilder = new StringBuilder();

      for (int i = 0; i < RESULT_FIELDS.length; i++) {
         if (RESULT_FIELDS[i] instanceof JSONObject) {
            JSONObject fieldObject = (JSONObject) RESULT_FIELDS[i];
            JSONObject innerObject = inputObject.optJSONObject(fieldObject.optString(KEY_INNER));
            String tmpKey = fieldObject.optString(KEY_VALUE);
            stringBuilder.append(innerObject.opt(tmpKey));
            stringBuilder.append(SEPARATOR);
         } else {
            stringBuilder.append(inputObject.optString((String) RESULT_FIELDS[i]));
            stringBuilder.append(SEPARATOR);
         }
      }

      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      stringBuilder.append(NEW_LINE);
      return stringBuilder.toString();
   }

   /**
    * get JSON data from URL_MAIN
    * 
    * @param cityName
    * @return
    * @throws IOException
    */
   private static JSONArray getJsonFromAPI(String cityName) throws IOException {
      InputStream response = null;
      Scanner scanner = null;
      try {
         URL_MAIN.append(URLEncoder.encode(cityName, URL_CHARSET));
         URLConnection connection = new URL(URL_MAIN.toString()).openConnection();
         connection.setRequestProperty("Accept-Charset", URL_CHARSET);
         response = connection.getInputStream();

         scanner = new Scanner(response, URL_CHARSET);
         return new JSONArray(scanner.useDelimiter("\\A").next());
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         throw new IOException("Connection error");
      } finally {
         if (response != null) {
            response.close();
         }
         if (scanner != null) {
            scanner.close();
         }
      }

   }

}

package com.perforce.team.ui.swarmreview.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class APIClient {

	private String URL;
	private String authScheme;
	private String authValue;
	private String method;
	private String body;
	private String disableAuth = System.getProperty("disableAuth", "false");
	private APIConnector connector = APIConnector.getConnector();

	public APIClient() {

	}

	public APIClient get(String URL) {
		this.URL = URL;
		method = "GET";
		return this;
	}
	
	public APIClient post(String URL, String body) {
		this.URL = URL;
		method = "POST";
		this.body = body;
		return this;
	}

	public APIClient head(String URL) {
		this.URL = URL;
		method = "HEAD";
		return this;
	}
	
	public APIClient withValues(String method, String URL, String params, String filter){
		this.method = method;
		this.URL = URL;
		return withQueryParams(params)
				.withFilter(filter);
	}
	
	private APIClient withQueryParams(String params){
		if("GET".equals(method)){
			if (params != null && !params.isEmpty()) {
				this.URL += "?" + params;
			}
		}
		
		if("POST".equals(method)){
			if (params != null && !params.isEmpty()) {
				this.body = params;
			}
		}
		return this;
	}
	
	private APIClient withFilter(String filter){
		if("GET".equals(method)){
			if (filter != null && !filter.isEmpty()) {
				URL += "&" + filter;
			}
		}
		return this;
	}

	public APIClient withAuth(AuthScheme scheme, String value) {
		if (!Boolean.parseBoolean(disableAuth)) {
			authScheme = scheme.getSchemeName();
			authValue = value;
		}
		return this;
	}

	

	/**
	 * Enables/Disables SSL validation with respect to an API call
	 * 
	 * @return
	 */
	public APIClient secure(boolean secure) {
		connector.setIgnoreSSLValidation(secure);
		return this;
	}

	public Response<String> doHEAD() throws IOException {
		HttpURLConnection connection = connect(URL);
		connection.setRequestMethod(method);
		int rescode = connection.getResponseCode();
		Response<String> response = new Response<String>(rescode, connection.getResponseMessage(), null);

		return response;
	}

	public Response<JsonString> ping(String URL) throws Exception {
		Response<JsonString> response = new Response<JsonString>(HttpURLConnection.HTTP_OK, "", new JsonString(""));
		response = get(URL).asJsonStringResponse();
		return response;
	}

	/**
	 * Perform POST request
	 * 
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	private void doPost(HttpURLConnection connection) throws IOException {
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept", "application/json");

		OutputStream os = connection.getOutputStream();
		os.write(body.getBytes());
		os.flush();
		// os.close();

	}

	/**
	 * Perform GET request
	 * 
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	private void doGet(HttpURLConnection connection) throws IOException {
		connection.setRequestProperty("Accept", "/");

	}

	/**
	 * Generates response
	 * 
	 * @param connection
	 * @param expectedResponse
	 * @return
	 * @throws Exception
	 */
	private Response<JsonString> generateJSONResponse(HttpURLConnection connection, Integer... expectedResponse)
			throws IOException {
		Response<JsonString> response;
		int rescode = connection.getResponseCode();
		String resMsg = connection.getResponseMessage();
		boolean validResponse = new ArrayList(Arrays.asList(expectedResponse)).contains(rescode);
		if (!validResponse) {
			JsonString js = prepareJsonString(connection.getErrorStream());
			response = new Response<JsonString>(rescode, resMsg, js);
		} else {
			JsonString js = prepareJsonString(connection.getInputStream());
			response = new Response<JsonString>(rescode, resMsg, js);
		}
		return response;
	}

	/**
	 * Generates response
	 * 
	 * @param connection
	 * @param expectedResponse
	 * @return
	 * @throws Exception
	 */
	private Response<String> generateStringResponse(HttpURLConnection connection, Integer... expectedResponse)
			throws IOException {
		Response<String> response;
		int rescode = connection.getResponseCode();
		String resMsg = connection.getResponseMessage();
		boolean validResponse = new ArrayList(Arrays.asList(expectedResponse)).contains(rescode);
		if (!validResponse) {
			String js = prepareString(connection.getErrorStream());
			response = new Response<String>(rescode, resMsg, js);
		} else {
			String js = prepareString(connection.getInputStream());
			response = new Response<String>(rescode, resMsg, js);
		}
		return response;
	}

	/**
	 * Prepares JsonString from the given InputStream
	 * 
	 * @param is
	 * @return
	 */
	private JsonString prepareJsonString(InputStream is) throws IOException {
		JsonString js = new JsonString("");
		if(is != null){
			BufferedReader br = new BufferedReader(new InputStreamReader((is)));
			try {
				Gson gson = new Gson();
				String jsonStr = ((JsonElement) gson.fromJson(br, JsonElement.class)).toString();
				js = new JsonString(jsonStr);
			} catch (JsonSyntaxException jsex) {
				js = new JsonString("Not in JSON format : " + prepareString(is));
			} catch (JsonIOException jioex) {
				js = new JsonString("Not in JSON format : " + prepareString(is));
			}
		}
		return js;
	}

	private String prepareString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader((is)));
		String streamVal = "";
		while ((streamVal = br.readLine()) != null) {
			sb.append(streamVal);
		}
		return streamVal;
	}

	private HttpURLConnection connect(String URL) throws IOException {
		connector.connect(URL);
		HttpURLConnection connection = connector.getConnection();
		if (connection == null) {
			throw new RuntimeException("Unable to connect");
		}
		return connection;
	}

	/**
	 * Returns the response as a JSON string
	 * 
	 * @return
	 */
	public Response<JsonString> asJsonStringResponse() throws APIException {
		Response<JsonString> response = null;
		try {
			HttpURLConnection connection = connect(URL);
			connection.setRequestMethod(method);

			if (authScheme != null && authValue != null && !Boolean.parseBoolean(disableAuth)) {
				String authEncoded = DatatypeConverter.printBase64Binary(authValue.getBytes());
				connection.setRequestProperty("Authorization", authScheme + " " + authEncoded);
			}

			if ("POST".equalsIgnoreCase(method)) {
				doPost(connection);
				response = generateJSONResponse(connection, HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_OK);

			} else {
				doGet(connection);
				response = generateJSONResponse(connection, HttpURLConnection.HTTP_OK);
				return response;
			}
		} catch (ConnectException cex) {
			throw new APIException("Could not connect to P4 Code Review server. ", cex, 0);
		} catch (ProtocolException ex) {
			throw new APIException("Invalid protocol", ex, 0);
		} catch (SSLHandshakeException ex) {
			throw new APIException("Unable to execute request over SSL. SSL handshake failed", ex, 0);
		} catch (IOException ex) {
			throw new APIException("Unable to talk to server", ex, 0);
		} finally {
			if(connector !=null && connector.getConnection() !=null)
			connector.getConnection().disconnect();
		}
		return response;
	}

	/**
	 * Returns the response as string
	 * 
	 * @return
	 */
	public Response<String> asStringResponse() throws APIException {
		Response<String> response = null;
		try {
			HttpURLConnection connection = connect(URL);
			connection.setRequestMethod(method);

			if (authScheme != null && authValue != null) {
				String authEncoded = DatatypeConverter.printBase64Binary(authValue.getBytes());
				connection.setRequestProperty("Authorization", authScheme + " " + authEncoded);
			}

			if ("POST".equalsIgnoreCase(method)) {
				doPost(connection);
				response = generateStringResponse(connection, HttpURLConnection.HTTP_CREATED,
						HttpURLConnection.HTTP_OK);
			} else {
				doGet(connection);
				response = generateStringResponse(connection, HttpURLConnection.HTTP_OK);
			}

		} catch (ConnectException ex) {
			throw new APIException("Could not connect to P4 Code Review server. ", ex, 0);
		} catch (ProtocolException ex) {
			throw new APIException("Invalid protocol", ex, 0);
		} catch (SSLHandshakeException ex) {
			throw new APIException("Unable to execute request over SSL. SSL handshake failed", ex, 0);
		} catch (IOException ex) {
			throw new APIException("Unable to talk to server", ex, 0);
		} finally {
			if(connector !=null && connector.getConnection() !=null)
			connector.getConnection().disconnect();
		}
		return response;
	}
}

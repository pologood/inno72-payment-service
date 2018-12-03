package com.inno72.payment.utils.wechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;


public class WechatCore {

	public static String makeXml(Map<String, String> params) {

		Set<String> keys = params.keySet();
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (String key : keys) {
			sb.append(String.format("<%s><![CDATA[%s]]></%s>", key, params.get(key), key));
		}
		sb.append("</xml>");
		return sb.toString();
	}

	public static String connectWechatWithSecret(String url, String xmlData, String mchId, String certPath)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			KeyManagementException, UnrecoverableKeyException {

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		InputStream instream = WechatCore.class.getResourceAsStream(certPath);
		try {
			keyStore.load(instream, mchId.toCharArray());
		} finally {
			instream.close();
		}
		SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchId.toCharArray())
				.build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity reqEntity = new StringEntity(xmlData, "utf-8");
			httpPost.setEntity(reqEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try {
				HttpEntity rspEntity = response.getEntity();
				if(rspEntity == null){
					throw new IOException("wechat entity is null");
				}
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rspEntity.getContent()));
				String tmp;
				StringBuilder sb = new StringBuilder();
				while ((tmp = bufferedReader.readLine()) != null) {
					sb.append(tmp);
                }
				return sb.toString();
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}

}

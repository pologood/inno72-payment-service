package com.inno72.payment.utils.wechat;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.inno72.payment.common.Constants;

public class WechatXmlParse extends DefaultHandler{
	
	private Map<String, String> elements = new HashMap<String, String>();
	
	private String index = null;
	
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		
		if(index == null) return;
		
		if(elements.containsKey(index)) {
			String element = elements.get(index);
			element += new String(arg0, arg1, arg2);
			elements.put(index, element);
		}else {
			String element = new String(arg0, arg1, arg2);
			elements.put(index, element);
		}
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		this.index = qName;
		
	}
	
	public Map<String, String> getElements() {
		return elements;
	}
	
	
	public static  Map<String, String> parse(String xml) throws Exception {
		
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		
		WechatXmlParse wechatXmlParse = new WechatXmlParse();
		
		parser.parse(new ByteArrayInputStream(xml.getBytes(Constants.SERVICE_CHARSET)), wechatXmlParse);
		
		return wechatXmlParse.getElements();
	}
	
}

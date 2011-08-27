package cn.kyle.bpswswitcher;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import cn.kyle.util.Tool;
import android.content.Context;
import android.content.res.XmlResourceParser;

public class Baseband {
	public String id;
	public String name;
	public String text;
	public String wcdma;
	public String versionFull;
	public String version;
	public String path;
	private String mac;
	
	public static List<Baseband> parseFromXml(Context context) {
		LinkedList<Baseband> bs = new LinkedList<Baseband>();
		int resid = R.xml.basebands;//设置主界面的菜单语言
		if (context.getResources().getConfiguration().locale.getLanguage().equals(Locale.CHINESE.getLanguage())){
			// 所的的值将会自动保存到SharePreferences
			resid= R.xml.basebands_chs;
		}
		XmlResourceParser xrp = context.getResources().getXml(resid);
		try {
			xrp.next();
			int eventType = xrp.getEventType();
			String id=null;
			Baseband bb = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					
				} else if (eventType == XmlPullParser.START_TAG) {
					if (xrp.getName().equals("baseband")){
						bb = new Baseband();
						bb.id = xrp.getIdAttribute();
						bb.name = xrp.getAttributeValue(null, "name");
						bb.text = xrp.getAttributeValue(null, "text");
						bb.wcdma = xrp.getAttributeValue(null, "wcdma");
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if (xrp.getName().equals("baseband")){
						bs.add(bb);
					}
				} else if (eventType == XmlPullParser.TEXT) {
					
				}
				eventType = xrp.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bs;
	}
	
	public String toString(){
		return "{"+this.id+","+this.name+","+this.text+","+this.wcdma+"}";
	}
	
	public String getMac(){
		if (mac==null){
			//mac = new String(Tool.macFile(new File(path,Module.BasebandFile.File_GSM.toString())));
			mac=""+this.hashCode();
		}
		return mac;
	}
}

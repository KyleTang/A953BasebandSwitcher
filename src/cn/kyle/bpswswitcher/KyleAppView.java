package cn.kyle.bpswswitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import cn.kyle.util.KyleApp;
import cn.kyle.util.L;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;


public class KyleAppView extends Activity {
    /** Called when the activity is first created. */
    
    public ProgressDialog pBar;
	private Toast myToast = null;
	private Handler handler = new Handler();
	private String moreAppUrl = "http://124.207.182.168:9080/downapk/kyleapp.xml";
	private String moreAppTmpXml = "/mnt/sdcard/kyleapp_tmp.xml";
	private LinkedList<KyleApp> appList = new LinkedList<KyleApp>();
	private LinearLayout llAppList = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appview);
        llAppList = (LinearLayout)findViewById(R.id.llAppList);
        loadAppList();
    }
    
    public void loadAppList(){
    	DownFileError dfe = null;
    	if (isNetworkAvailable()){
    		dfe = downFile(moreAppUrl,moreAppTmpXml);
    	}else{
    		dfe = DownFileError.None;
    	}
    	
    	if (dfe == DownFileError.None && new File(moreAppTmpXml).exists()){
    		appList.clear();
    		appList.addAll(KyleApp.parseXml(moreAppTmpXml));
    	}
    	
    	TextView tvTip = new TextView(this);
    	tvTip.setText(!isNetworkAvailable()?"提示：网络不可用":
    		appList.size()>0?"提示：长按开始下载":"提示：暂无");
    	llAppList.removeAllViews();
    	llAppList.addView(tvTip);
    	
    	for (int i=0;i<appList.size();i++){
    		final TextView tv = new TextView(this);
    		tv.setTag(appList.get(i));
    		tv.setText(appList.get(i).getAppText());
    		//tv.setBackgroundColor(0xF0FFFF);
    		tv.setBackgroundResource(android.R.drawable.btn_default);
    		MarginLayoutParams tvPara = new MarginLayoutParams(
    				ViewGroup.LayoutParams.FILL_PARENT,
    				ViewGroup.LayoutParams.WRAP_CONTENT);
    		tvPara.setMargins(0, 10, 0, 10);
    		tv.setLayoutParams(tvPara);

    		tv.setOnLongClickListener(new OnLongClickListener(){
				public boolean onLongClick(View v) {
					final KyleApp k = (KyleApp)tv.getTag();
					
					pBar = new ProgressDialog(KyleAppView.this);
					pBar.setTitle("正在下载 ["+k.name_ZH+"]");
					pBar.setMessage("请稍候...");
					pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					pBar.show();
					new Thread() {
						public void run() {
							if (downFile(k.downLink,k.getTmpFileName())==DownFileError.None){
								handlerDownloadFinished(k.getTmpFileName());
							}else{
								handlerDownloadFailed("下载["+k.name_ZH+"]安装文件失败！");
							}
						}
					}.start();
					return false;
				}
    		});
    		llAppList.addView(tv);
    	}
    }
    
	void install(String filename) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/" + filename)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}
    
    boolean isNetworkAvailable() {
		try {
			ConnectivityManager cm = (ConnectivityManager) this
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			return (info != null && info.isConnected());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    
	void handlerDownloadFinished(final String filename) {
		handler.post(new Runnable() {
			public void run() {
				if (pBar != null)
					pBar.cancel();
				install(filename);
			}
		});
	}

	void handlerDownloadFailed(final String errorInfo) {
		handler.post(new Runnable() {
			public void run() {
				AlertDialog d = new AlertDialog.Builder(KyleAppView.this)
						.setTitle("警告").setMessage(errorInfo)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
															
							}
						})
						.create();
				d.show();
			}
		});
	}    
    DownFileError downFile(final String url, final String tmpFile) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		L.debug("downFile - begin URL=" + url);
		long startDown = System.currentTimeMillis();
		HttpResponse response = null;
		DownFileError result = null;
		try {
			response = client.execute(get);
			//client.execute 执行失败会跑异常，不会有空指针，所以response不需要判断
			if (response.getStatusLine()==null||response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
				if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_NOT_FOUND){
					return DownFileError.FileNotFound;
				}
				return DownFileError.InvalidURL;
			}
			result = DownFileError.None;
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			InputStream is = entity.getContent();
			FileOutputStream fileOutputStream = null;
			if (is != null) {
				File file = null;
				if (tmpFile.startsWith("/"))
					file = new File(tmpFile);
				else
					file = new File(Environment.getExternalStorageDirectory(),
						tmpFile);
				if (file.exists())
					file.delete();
				fileOutputStream = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int ch = -1;
				int count = 0;
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
					count += ch;
					if (length > 0) {
					}
				}
			}
			fileOutputStream.flush();
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			L.debug("downFile - end , time= "
							+ (System.currentTimeMillis() - startDown)
							+ ", URL=" + url);
			return DownFileError.None;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			L.debug("downFile URL=" + url + ", IOException=" + e.getMessage());
			e.printStackTrace();
			if(result==null)
				return DownFileError.NotAvailableServer; /*连接超时*/
			else
				return DownFileError.DownloadBreak;
		}
		return DownFileError.NotHttpServer;
	}
    
	public static enum DownFileError{
		None("成功"),
		NotAvailableServer("服务器不可用"),
		NotHttpServer("不是HTTP服务器"),
		InvalidURL("无效的URL地址"),
		DownloadBreak("下载中断，请检查网络"),
		FileNotFound("文件不存在");
		
		final String info;
		DownFileError(String info){
			this.info = info;
		}
		public String toString(){
			return info;
		}
	}
	
}

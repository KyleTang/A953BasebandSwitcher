package cn.kyle.bpswswitcher;

import java.util.LinkedList;

import cn.kyle.bpswswitcher.Module.BPSW;
import cn.kyle.bpswswitcher.Module.BpswCompareResult;
import cn.kyle.util.G;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class Switcher extends Activity {
    /** Called when the activity is first created. */
	private TextView tvTip = null;
	private Toast myToast = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        G.setResourcesAndContext(this);
        
        tvTip = (TextView)findViewById(R.id.tvTip);
        this.setTitle(this.getTitle()+" "+G.versionName);
        
        Button btnBackUp = (Button)findViewById(R.id.btnBackUp);
        btnBackUp.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if (Module.backup()){
					AlertDialog a = new AlertDialog.Builder(Switcher.this)
					.setTitle("提示").setMessage("备份成功！")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).create();
					a.show();
				}else{
					AlertDialog a = new AlertDialog.Builder(Switcher.this)
					.setTitle("警告").setMessage("备份失败！")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).create();
					a.show();
				}
			}
        });
        
        Button btnRestore = (Button)findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("还原为", Module.checkBackupBpsw() , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.restore();
					}
				});
			}
        });
        
        Button btnA953UK = (Button)findViewById(R.id.btnA953UK);
        btnA953UK.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("切换为", Module.BPSW.a953_uk , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.toA953UK();
					}
				});
			}
        });
        
        Button btnA953AuVodafone = (Button)findViewById(R.id.btnA953AuVodafone);
        btnA953AuVodafone.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("切换为", Module.BPSW.a953_au_vodafone , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.toA953_Au_Vodafone();
					}
				});
			}
        });
        
        Button btnA953Brazil = (Button)findViewById(R.id.btnA953Brazil);
        btnA953Brazil.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("切换为", Module.BPSW.a953_brazil , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.toA953_Brazil();
					}
				});
			}
        });
        
        Button btnME722ZHCN = (Button)findViewById(R.id.btnME722ZHCN);
        btnME722ZHCN.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("切换为", Module.BPSW.me722_zhcn , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.toME722ZHCN();
					}
				});
			}
        });
        
        Button btnME722ZHCN_new = (Button)findViewById(R.id.btnME722ZHCN_new);
        btnME722ZHCN_new.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doSwitch("切换为", Module.BPSW.me722_zhcn_new , new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.toME722ZHCN_New();
					}
				});
			}
        });
        
        refreshTip();
    }

    public void doSwitch(String action, Module.BPSW  toBpsw, final DialogInterface.OnClickListener onClickAction ){
    	if (!G.haveRoot()){
    		AlertDialog a = new AlertDialog.Builder(Switcher.this)
    		.setTitle("警告").setMessage("手机需要先破解ROOT权限！")
    		.setPositiveButton("确定", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which) {
    			}
    		}).create();
    		a.show();
    	}else{
	    	AlertDialog a = new AlertDialog.Builder(Switcher.this)
			.setTitle("提示").setMessage("是否要"+action+toBpsw.getName())
			.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					onClickAction.onClick(dialog, which);
					refreshTip();
					AlertDialog aa = new AlertDialog.Builder(Switcher.this)
		    		.setTitle("提示").setMessage("切换成功")
		    		.setPositiveButton("确定", new DialogInterface.OnClickListener(){
		    			public void onClick(DialogInterface dialog, int which) {
		    			}
		    		}).create();
		    		aa.show();
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).create();
			a.show();
    	}
    }
    
    public void refreshTip(){
    	String current = Module.loadBpswVersionStr(true);
    	if (current.contains("MILESTONE2")){
    		current = "(里程碑2专用) "+current.substring(current.indexOf("MILESTONE2")+"MILESTONE2".length());
    	}else if (current.contains("JRDNEM")){
    		current = "(defy专用) "+current.substring(current.indexOf("JRDNEM")+"JRDNEM".length());
    	}
    	String backup = Module.hasBackup()?Module.loadBpswVersionStr(false):"-";
    	if (backup.contains("MILESTONE2")){
    		backup = "(里程碑2专用) "+backup.substring(backup.indexOf("MILESTONE2")+"MILESTONE2".length());
    	}else if (backup.contains("JRDNEM")){
    		backup = "(defy专用) "+backup.substring(backup.indexOf("JRDNEM")+"JRDNEM".length());
    	}
    	tvTip.setText(
    			"当前系统的基带为："+Module.checkCurrentBpsw().getName()+"\n"+
    			"版本号："+current+"\n\n"+
    			"当前备份的基带为："+(Module.hasBackup()?Module.checkBackupBpsw().getName():"(尚未备份)")+"\n"+
    			"版本号："+backup);
    }
    
	public final int MENU_ABOUT = 0;
	public final int MENU_MOREAPP = 1;

	private MenuItem miAbout= null;
	private MenuItem miMoreApp = null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int groupId = 0;
		int menuItemOrder = Menu.NONE;
		menu.add(groupId, MENU_ABOUT, menuItemOrder,"关于");
		menu.add(groupId, MENU_MOREAPP, menuItemOrder,"更多应用");
		
		miAbout = menu.findItem(MENU_ABOUT);
		miMoreApp = menu.findItem(MENU_MOREAPP);
		
		miAbout.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				myToast("名称：里程碑2基带切换器\n" +
						"版本："+G.versionName+"\n"+
						"作者：Kyle Tang \n" +
						"机锋ID：sw_acer");
				return false;
			}
		});
		
		miMoreApp.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(Switcher.this,KyleAppView.class);
				startActivity(i);
				return false;
			}
		});

		return true;
	
	}
	
	public void myToast(String tipInfo) {
		if (myToast == null)
			myToast = new Toast(Switcher.this);
		myToast.makeText(Switcher.this, tipInfo, Toast.LENGTH_SHORT).show();
	}
}
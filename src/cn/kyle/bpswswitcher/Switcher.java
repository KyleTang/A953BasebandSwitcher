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
		    		.setTitle("提示").setMessage("切换成功，重启后生效，是否立即重启？")
		    		.setPositiveButton("是", new DialogInterface.OnClickListener(){
		    			public void onClick(DialogInterface dialog, int which) {
		    				G.execRootCmdSilent("reboot");
		    			}
		    		})
		    		.setNeutralButton("否", new DialogInterface.OnClickListener(){
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
    
    public static String resolvingVersion(String verstr){
    	if (verstr.contains("MILESTONE2")){
    		return "(里程碑2专用) "+verstr.substring(verstr.indexOf("MILESTONE2")+"MILESTONE2".length());
    	}
//		File_GSMAGA:			US1MILESTONE2PERARB125LA016.0R
//		File_GSMAO:				UCA JRDNEMARA B1B80AA035.0R
//		File_GSMAT:				USA JRDNEMARA B1B5TEL02A.0R
//		File_GSMBC:				USA JORD B15CLABRLA011.0R
//		File_GSMC:				USA JRDNPRC B1B50AA02F.0R
//		File_GSMCEE:			UCA JRDNEMARA B1B80AA02B.0R
//		File_GSMCEE3.4.2:		UCA JRDNEMARA B1B80AA03A.0R
//		File_GSMEU:				UCA JRDNEMARA B1B80AA039.0R
//		File_GSMF:				UCA JRDNEMARA B1B80AA028.0R
//		File_GSMHTC:			UCA JRDNEMARA B1B80AA030.0R
//		File_GSMINT:			UCA JRDNEMARA B1B50AA028.0R
//		File_GSMPO:				USA JRDNEMARA B1B8ORAPL035.0R
//		File_GSMUK2.51.1:		USA JRDNEMARA B1B8RTGB02C.0R
//		File_GSMUK3.4.2-117:	USA JRDNEMARA B1B8RTGB035.0R
//		File_GSMUK3.4.3-3:		USA JRDNEMARA B1B8RTGB039.0R
//		File_GSMUKT_2.21.1:		USA JRDNEMARA B1B8TMGB028.0R
//		File_GSMUKT_2.51.1:		USA JRDNEMARA B1B8TMGB030.0R
//		File_GSMUST3.4.2-107:	USA JRDNTMO B1B4B5DE1035.0R
//		File_GSMUST3.4.2-107-9:	USA JRDNTMO B1B4B5DE1039.0R
//		File_GSMUST6.19.0:		USA JRDNTMO B1B4B5DE1028.0R
    	if (verstr.contains("JRDNEMARA")){
    		return "(defy专用) JRDNEMARA "+verstr.substring(verstr.indexOf("JRDNEMARA")+"JRDNEMARA".length());
    	}
    	if (verstr.contains("JORD")){
    		return "(defy专用) JORD "+verstr.substring(verstr.indexOf("JORD")+"JORD".length());
    	}
    	if (verstr.contains("JRDNPRC")){
    		return "(defy专用) 国行  "+verstr.substring(verstr.indexOf("JRDNPRC")+"JRDNPRC".length());
    	}
    	if (verstr.contains("JRDNTMO")){
    		return "(defy专用) JRDNTMO "+verstr.substring(verstr.indexOf("JRDNTMO")+"JRDNTMO".length());
    	}
    	
    	if (verstr.contains("JRDN")){
    		return "(defy专用) JRDN "+verstr.substring(verstr.indexOf("JRDN")+"JRDN".length());
    	}
    	return verstr;
    }
    
    public void refreshTip(){
    	String current = Module.loadBpswVersionStr(true);
    	current = resolvingVersion(current);
    	String backup = Module.hasBackup()?Module.loadBpswVersionStr(false):"-";
    	backup = resolvingVersion(backup);
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
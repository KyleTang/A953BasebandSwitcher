package cn.kyle.bpswswitcher;

import java.util.Iterator;
import java.util.LinkedList;

import cn.kyle.bpswswitcher.Module.BpswCompareResult;
import cn.kyle.util.G;
import cn.kyle.util.L;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Switcher extends Activity {
    /** Called when the activity is first created. */
	private TextView tvTip = null;
	private Toast myToast = null;
	private LinearLayout llBasebands = null; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        G.setResourcesAndContext(this);
       // G.execRootCmdSilent(Module.sysrw);
        tvTip = (TextView)findViewById(R.id.tvTip); 
        llBasebands = (LinearLayout)findViewById(R.id.llBasebands);
        this.setTitle(this.getTitle()+" "+G.versionName);

        addButtons();
        
        refreshTip();
    }
    
    private void addButtons() {
		Button btn = null;
		Module.basebands = Baseband.parseFromXml(this);
		for (Iterator<Baseband> itr = Module.basebands.iterator(); itr
				.hasNext();) {
			Baseband bb = itr.next();
			L.debug("baseband:" + bb.toString());
			btn = new Button(this);
			btn.setTag(bb);
			btn.setText(bb.name+"  "+bb.wcdma+""+(
					bb.text.length()==0?"":"\n("+bb.text.trim()+")" ));
			btn.setOnClickListener((OnClickListener) new OnButtonClickListener(btn));
			llBasebands.addView(btn);
		}
	}
    
    private class OnButtonClickListener implements android.view.View.OnClickListener{
    	private Button btn;
    	public OnButtonClickListener(Button btn){
    		this.btn = btn;
    	}
		public void onClick(View v) {
			//switch baseband
			final Baseband bb = (Baseband)btn.getTag();
			doSwitch("切换为 ", bb , new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					Module.switchTo(bb.id);
					refreshTip();
				}
			});
			
		}
    	
    }
    
    
    /**
     * @param action
     * @param toBpsw
     * @param onClickAction
     */
    public void doSwitch(String action, Baseband t, final DialogInterface.OnClickListener onClickAction ){
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
			.setTitle("提示").setMessage("是否要"+action+(t==null?"备份基带":t.name))
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
    
    public void refreshTip(){
    	String current = Module.getVersionById(Module.ID_SYSTEM);
    	current = Module.resolvingVersion(current);
    	String backup = Module.hasBackup()?Module.getVersionById(Module.ID_BACKUP):"-";
    	backup = Module.resolvingVersion(backup);
    	Baseband bc = Module.checkCurrentBaseband();
    	Baseband bb = Module.checkBackupBaseband();
    	tvTip.setText(
    			"当前系统的基带为："+(bc==null?"(未知基带)":bc.name)+"\n"+
    			"版本号："+current+"\n\n"+
    			"当前备份的基带为："+(Module.hasBackup()? (bb==null?"(未知基带)":bb.name):"(尚未备份)")+"\n"+
    			"版本号："+backup);
    }
    
	public final int MENU_BACKUP = 0;
	public final int MENU_RESTORE = 1;
	public final int MENU_ABOUT = 2;

	private MenuItem miAbout= null;
	private MenuItem miBackup = null;
	private MenuItem miRestore = null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int groupId = 0;
		int menuItemOrder = Menu.NONE;
		menu.add(groupId, MENU_BACKUP, menuItemOrder,"备份");
		menu.add(groupId, MENU_RESTORE, menuItemOrder,"还原");
		menu.add(groupId, MENU_ABOUT, menuItemOrder,"关于");
		
		
		miAbout = menu.findItem(MENU_ABOUT);
		miBackup = menu.findItem(MENU_BACKUP);
		miRestore = menu.findItem(MENU_RESTORE);
		
		miAbout.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				myToast("名称：里程碑2基带切换器\n" +
						"版本："+G.versionName+"\n"+
						"作者：Kyle Tang \n" +
						"机锋ID：sw_acer");
				return false;
			}
		});
		
		miBackup.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
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
				refreshTip();
				return false;
			}
		});
		
		miRestore.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				doSwitch("还原为 ", Module.checkBackupBaseband(), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						Module.restore();
						refreshTip();
					}
				});
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
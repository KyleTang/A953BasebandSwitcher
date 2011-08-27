package cn.kyle.bpswswitcher;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import cn.kyle.bpswswitcher.Module.BpswCompareResult;
import cn.kyle.util.G;
import cn.kyle.util.L;
import cn.kyle.util.MultiLang;
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
	private MultiLang ml = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ml = new MultiLang(this);
        Module.setMultiLang(ml);
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
			doSwitch(ml.t(R.string.text_switchto, null), bb , new DialogInterface.OnClickListener(){
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
    		.setTitle(R.string.dialog_title_warning).setMessage(R.string.msg_needroot)
    		.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which) {
    			}
    		}).create();
    		a.show();
    	}else{
	    	AlertDialog a = new AlertDialog.Builder(Switcher.this)
			.setTitle(R.string.dialog_title_tip).setMessage(
						ml.t(R.string.msg_doyouneed, 
							new String[]{
								action,
								(t==null?ml.t(R.string.text_backup, null):t.name)}
						)
				)
			.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					onClickAction.onClick(dialog, which);
					refreshTip();
					AlertDialog aa = new AlertDialog.Builder(Switcher.this)
		    		.setTitle(R.string.dialog_title_tip).setMessage(R.string.msg_rebootNow)
		    		.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener(){
		    			public void onClick(DialogInterface dialog, int which) {
		    				G.execRootCmdSilent("reboot");
		    			}
		    		})
		    		.setNeutralButton(R.string.btn_no, new DialogInterface.OnClickListener(){
		    			public void onClick(DialogInterface dialog, int which) {
		    			}
		    		}).create();
		    		aa.show();
				}
			})
			.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener(){
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
    			ml.t(R.string.text_tip_currentSystem, null)+(bc==null?ml.t(R.string.text_unknown, null):bc.name)+"\n"+
    			ml.t(R.string.text_tip_version, null)+current+"\n\n"+
    			ml.t(R.string.text_tip_currentBackup, null)+(Module.hasBackup()? (bb==null?ml.t(R.string.text_unknown, null):bb.name):ml.t(R.string.text_noBackup, null))+"\n"+
    			ml.t(R.string.text_tip_version, null)+backup);
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
		menu.add(groupId, MENU_BACKUP, menuItemOrder,R.string.menu_backup);
		menu.add(groupId, MENU_RESTORE, menuItemOrder,R.string.menu_restore);
		menu.add(groupId, MENU_ABOUT, menuItemOrder,R.string.menu_about);
		
		
		miAbout = menu.findItem(MENU_ABOUT);
		miBackup = menu.findItem(MENU_BACKUP);
		miRestore = menu.findItem(MENU_RESTORE);
		
		miAbout.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				myToast(ml.t(R.string.text_about,new String[]{ml.t(R.string.app_name,null),G.versionName,"Kyle Tang","sw_acer"}));
				return false;
			}
		});
		
		miBackup.setOnMenuItemClickListener(new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				if (Module.backup()){
					AlertDialog a = new AlertDialog.Builder(Switcher.this)
					.setTitle(R.string.dialog_title_tip).setMessage(R.string.msg_backupOK)
					.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).create();
					a.show();
				}else{
					AlertDialog a = new AlertDialog.Builder(Switcher.this)
					.setTitle(R.string.dialog_title_warning).setMessage(R.string.msg_backupFail)
					.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
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
				doSwitch(ml.t(R.string.text_restore, null), Module.checkBackupBaseband(), new DialogInterface.OnClickListener(){
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
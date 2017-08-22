package extend.plugn.takephoto;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.util.ArrayMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.R;
import extend.plugn.utils.ReflectUtils;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper;
import io.rong.imkit.plugin.image.PicturePreviewActivity;
import io.rong.imkit.plugin.image.PictureSelectorActivity;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.RongUtils;

public class PictureSelectorActivityEx extends Activity {
    public static final int REQUEST_PREVIEW = 0;
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    private GridView mGridView;
    private ImageButton mBtnBack;
    private Button mBtnSend;
    private PictureSelectorActivity.PicTypeBtn mPicType;
    private PictureSelectorActivity.PreviewBtn mPreviewBtn;
    private View mCatalogView;
    private ListView mCatalogListView;
    private List<PictureSelectorActivity.PicItem> mAllItemList;
    private Map<String, List<PictureSelectorActivity.PicItem>> mItemMap;
    private List<String> mCatalogList;
    private String mCurrentCatalog = "";
    private Uri mTakePictureUri;
    private boolean mSendOrigin = false;
    private int perWidth;

    public PictureSelectorActivityEx() {
    }

    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        this.setContentView(io.rong.imkit.R.layout.rc_picsel_activity);
        if(savedInstanceState != null) {
            PictureSelectorActivity.PicItemHolder.itemList = savedInstanceState.getParcelableArrayList("ItemList");
        }

        this.mGridView = (GridView)this.findViewById(io.rong.imkit.R.id.gridlist);
        this.mBtnBack = (ImageButton)this.findViewById(io.rong.imkit.R.id.back);
        this.mBtnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PictureSelectorActivityEx.this.finish();
            }
        });
        this.mBtnSend = (Button)this.findViewById(io.rong.imkit.R.id.send);
        this.mPicType = (PictureSelectorActivity.PicTypeBtn)this.findViewById(io.rong.imkit.R.id.pic_type);
        this.mPicType.init(this);
        this.mPicType.setEnabled(false);
        this.mPreviewBtn = (PictureSelectorActivity.PreviewBtn)this.findViewById(io.rong.imkit.R.id.preview);
        this.mPreviewBtn.init(this);
        this.mPreviewBtn.setEnabled(false);
        this.mCatalogView = this.findViewById(io.rong.imkit.R.id.catalog_window);
        this.mCatalogListView = (ListView)this.findViewById(io.rong.imkit.R.id.catalog_listview);
        String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        if(!PermissionCheckUtil.checkPermissions(this, permissions)) {
            PermissionCheckUtil.requestPermissions(this, permissions, 100);
        } else {
            this.initView();
        }
    }

    private void initView() {
        this.updatePictureItems();
        this.mGridView.setAdapter(new PictureSelectorActivityEx.GridViewAdapter());
        this.mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if(position != 0) {
                    PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
                    if(PictureSelectorActivityEx.this.mCurrentCatalog.isEmpty()) {
                        PictureSelectorActivity.PicItemHolder.itemList.addAll(PictureSelectorActivityEx.this.mAllItemList);
                    } else {
                        PictureSelectorActivity.PicItemHolder.itemList.addAll((Collection) PictureSelectorActivityEx.this.mItemMap.get(PictureSelectorActivityEx.this.mCurrentCatalog));
                    }

                    Intent intent = new Intent(PictureSelectorActivityEx.this, PicturePreviewActivity.class);
                    intent.putExtra("index", position);
                    intent.putExtra("sendOrigin", PictureSelectorActivityEx.this.mSendOrigin);
                    PictureSelectorActivityEx.this.startActivityForResult(intent, 0);
//                }
            }
        });
        this.mBtnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                ArrayList list = new ArrayList();
                Iterator var4 = PictureSelectorActivityEx.this.mItemMap.keySet().iterator();

                while(var4.hasNext()) {
                    String key = (String)var4.next();
                    Iterator var6 = ((List) PictureSelectorActivityEx.this.mItemMap.get(key)).iterator();

                    while(var6.hasNext()) {
                        PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var6.next();
                        if(isItemSelected(item)) {
                            list.add(Uri.parse("file://" + itemUri(item)));
                        }
                    }
                }

                data.putExtra("sendOrigin", PictureSelectorActivityEx.this.mSendOrigin);
                data.putExtra("android.intent.extra.RETURN_RESULT", list);
                PictureSelectorActivityEx.this.setResult(-1, data);
                PictureSelectorActivityEx.this.finish();
            }
        });
        this.mPicType.setEnabled(true);
        this.mPicType.setTextColor(this.getResources().getColor(io.rong.imkit.R.color.rc_picsel_toolbar_send_text_normal));
        this.mPicType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PictureSelectorActivityEx.this.mCatalogView.setVisibility(View.VISIBLE);
            }
        });
        this.mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
                Iterator intent = PictureSelectorActivityEx.this.mItemMap.keySet().iterator();

                while(intent.hasNext()) {
                    String key = (String)intent.next();
                    Iterator var4 = ((List) PictureSelectorActivityEx.this.mItemMap.get(key)).iterator();

                    while(var4.hasNext()) {
                        PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var4.next();
                        if(isItemSelected(item)) {
                            PictureSelectorActivity.PicItemHolder.itemList.add(item);
                        }
                    }
                }

                Intent intent1 = new Intent(PictureSelectorActivityEx.this, PicturePreviewActivity.class);
                intent1.putExtra("sendOrigin", PictureSelectorActivityEx.this.mSendOrigin);
                PictureSelectorActivityEx.this.startActivityForResult(intent1, 0);
            }
        });
        this.mCatalogView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == 1 && PictureSelectorActivityEx.this.mCatalogView.getVisibility() == View.VISIBLE) {
                    PictureSelectorActivityEx.this.mCatalogView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        this.mCatalogListView.setAdapter(new PictureSelectorActivityEx.CatalogAdapter());
        this.mCatalogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String catalog;
//                if(position == 0) {
//                    catalog = "";
//                } else {
                    catalog = (String) PictureSelectorActivityEx.this.mCatalogList.get(position);
//                }

                if(catalog.equals(PictureSelectorActivityEx.this.mCurrentCatalog)) {
                    PictureSelectorActivityEx.this.mCatalogView.setVisibility(View.GONE);
                } else {
                    PictureSelectorActivityEx.this.mCurrentCatalog = catalog;
                    TextView textView = (TextView)view.findViewById(R.id.name);
                    PictureSelectorActivityEx.this.mPicType.setText(textView.getText().toString());
                    PictureSelectorActivityEx.this.mCatalogView.setVisibility(View.GONE);
                    ((PictureSelectorActivityEx.CatalogAdapter) PictureSelectorActivityEx.this.mCatalogListView.getAdapter()).notifyDataSetChanged();
                    ((PictureSelectorActivityEx.GridViewAdapter) PictureSelectorActivityEx.this.mGridView.getAdapter()).notifyDataSetChanged();
                }
            }
        });
        this.perWidth = (((WindowManager)((WindowManager)this.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth() - RongUtils.dip2px(4.0F)) / 3;
    }

    @TargetApi(23)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100) {
            if(Build.VERSION.SDK_INT >= 23 && this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
                this.initView();
            } else {
                Toast.makeText(this.getApplicationContext(), this.getString(io.rong.imkit.R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }

        if(resultCode != 0) {
            if(resultCode == 1) {
                this.setResult(-1, data);
                this.finish();
            } else {
                switch(requestCode) {
                    case 0:
                        this.mSendOrigin = data.getBooleanExtra("sendOrigin", false);
                        ArrayList item2 = PictureSelectorActivity.PicItemHolder.itemList;
                        Iterator intent1 = item2.iterator();

                        while(intent1.hasNext()) {
                            PictureSelectorActivity.PicItem it = (PictureSelectorActivity.PicItem)intent1.next();
                            PictureSelectorActivity.PicItem item1 = this.findByUri(itemUri(it));
                            if(item1 != null) {
                                itemSelectedValue(item1, isItemSelected(it));
                            }
                        }

                        ((PictureSelectorActivityEx.GridViewAdapter)this.mGridView.getAdapter()).notifyDataSetChanged();
                        ((PictureSelectorActivityEx.CatalogAdapter)this.mCatalogListView.getAdapter()).notifyDataSetChanged();
                        this.updateToolbar();
                        break;
                    case 1:
                        if(this.mTakePictureUri != null) {
                            PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
                            PictureSelectorActivity.PicItem item = new PictureSelectorActivity.PicItem();
                            itemUriValue(item,this.mTakePictureUri.getPath());
                            PictureSelectorActivity.PicItemHolder.itemList.add(item);
                            Intent intent = new Intent(this, PicturePreviewActivity.class);
                            this.startActivityForResult(intent, 0);
                            MediaScannerConnection.scanFile(this, new String[]{this.mTakePictureUri.getPath()}, (String[])null, new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    PictureSelectorActivityEx.this.updatePictureItems();
                                }
                            });
                        }
                }

            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 4 && this.mCatalogView != null && this.mCatalogView.getVisibility() == View.VISIBLE) {
            this.mCatalogView.setVisibility(View.GONE);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    protected void requestCamera() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(!path.exists()) {
            path.mkdirs();
        }

        String name = System.currentTimeMillis() + ".jpg";
        File file = new File(path, name);
        this.mTakePictureUri = Uri.fromFile(file);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        List resInfoList = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        Uri uri = null;

        try {
            uri = FileProvider.getUriForFile(this, this.getPackageName() + ".FileProvider", file);
        } catch (Exception var10) {
            var10.printStackTrace();
            throw new RuntimeException("Please check IMKit Manifest FileProvider config.");
        }

        Iterator e = resInfoList.iterator();

        while(e.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo)e.next();
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.putExtra("output", uri);
        this.startActivityForResult(intent, 1);
    }

    private void updatePictureItems() {
        String[] projection = new String[]{"_data", "date_added"};
        String orderBy = "datetaken DESC";
        Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, (String)null, (String[])null, orderBy);
        this.mAllItemList = new ArrayList();
        this.mCatalogList = new ArrayList();
        this.mItemMap = new ArrayMap();
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    PictureSelectorActivity.PicItem item = new PictureSelectorActivity.PicItem();
                    itemUriValue(item, cursor.getString(0));
                    if(itemUri(item) != null) {
                        this.mAllItemList.add(item);
                        int last = itemUri(item).lastIndexOf("/");
                        if(last != -1) {
                            String catalog;
                            if(last == 0) {
                                catalog = "/";
                            } else {
                                int itemList = itemUri(item).lastIndexOf("/", last - 1);
                                catalog = itemUri(item).substring(itemList + 1, last);
                            }

                            if(this.mItemMap.containsKey(catalog)) {
                                ((List)this.mItemMap.get(catalog)).add(item);
                            } else {
                                ArrayList itemList1 = new ArrayList();
                                itemList1.add(item);
                                this.mItemMap.put(catalog, itemList1);
                                this.mCatalogList.add(catalog);
                            }
                        }
                    }
                } while(cursor.moveToNext());
            }

            cursor.close();
        }

    }

    private int getTotalSelectedNum() {
        int sum = 0;
        Iterator var2 = this.mItemMap.keySet().iterator();

        while(var2.hasNext()) {
            String key = (String)var2.next();
            Iterator var4 = ((List)this.mItemMap.get(key)).iterator();

            while(var4.hasNext()) {
                PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var4.next();
                if(isItemSelected(item)) {
                    ++sum;
                }
            }
        }

        return sum;
    }

    private void updateToolbar() {
        int sum = this.getTotalSelectedNum();
        if(sum == 0) {
            this.mBtnSend.setEnabled(false);
            this.mBtnSend.setTextColor(this.getResources().getColor(io.rong.imkit.R.color.rc_picsel_toolbar_send_text_disable));
            this.mBtnSend.setText(io.rong.imkit.R.string.rc_picsel_toolbar_send);
            this.mPreviewBtn.setEnabled(false);
            this.mPreviewBtn.setText(io.rong.imkit.R.string.rc_picsel_toolbar_preview);
        } else if(sum <= 9) {
            this.mBtnSend.setEnabled(true);
            this.mBtnSend.setTextColor(this.getResources().getColor(io.rong.imkit.R.color.rc_picsel_toolbar_send_text_normal));
            this.mBtnSend.setText(String.format(this.getResources().getString(io.rong.imkit.R.string.rc_picsel_toolbar_send_num), new Object[]{Integer.valueOf(sum)}));
            this.mPreviewBtn.setEnabled(true);
            this.mPreviewBtn.setText(String.format(this.getResources().getString(io.rong.imkit.R.string.rc_picsel_toolbar_preview_num), new Object[]{Integer.valueOf(sum)}));
        }

    }

    private PictureSelectorActivity.PicItem getItemAt(int index) {
        int sum = 0;
        Iterator var3 = this.mItemMap.keySet().iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();

            for(Iterator var5 = ((List)this.mItemMap.get(key)).iterator(); var5.hasNext(); ++sum) {
                PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var5.next();
                if(sum == index) {
                    return item;
                }
            }
        }

        return null;
    }

    private PictureSelectorActivity.PicItem getItemAt(String catalog, int index) {
        if(!this.mItemMap.containsKey(catalog)) {
            return null;
        } else {
            int sum = 0;

            for(Iterator var4 = ((List)this.mItemMap.get(catalog)).iterator(); var4.hasNext(); ++sum) {
                PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var4.next();
                if(sum == index) {
                    return item;
                }
            }

            return null;
        }
    }

    private PictureSelectorActivity.PicItem findByUri(String uri) {
        Iterator var2 = this.mItemMap.keySet().iterator();

        while(var2.hasNext()) {
            String key = (String)var2.next();
            Iterator var4 = ((List)this.mItemMap.get(key)).iterator();

            while(var4.hasNext()) {
                PictureSelectorActivity.PicItem item = (PictureSelectorActivity.PicItem)var4.next();
                if(itemUri(item).equals(uri)) {
                    return item;
                }
            }
        }

        return null;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 100:
                if(grantResults[0] == 0) {
                    if(permissions[0].equals("android.permission.READ_EXTERNAL_STORAGE")) {
                        this.initView();
                    } else if(permissions[0].equals("android.permission.CAMERA")) {
                        this.requestCamera();
                    }
                } else if(permissions[0].equals("android.permission.CAMERA")) {
                    Toast.makeText(this.getApplicationContext(), this.getString(io.rong.imkit.R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this.getApplicationContext(), this.getString(io.rong.imkit.R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        if(PictureSelectorActivity.PicItemHolder.itemList != null && PictureSelectorActivity.PicItemHolder.itemList.size() > 0) {
            outState.putParcelableArrayList("ItemList", PictureSelectorActivity.PicItemHolder.itemList);
        }

        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        PictureSelectorActivity.PicItemHolder.itemList = null;
        super.onDestroy();
    }

    private class CatalogAdapter extends BaseAdapter {
        private LayoutInflater mInflater = PictureSelectorActivityEx.this.getLayoutInflater();

        public CatalogAdapter() {
        }

        public int getCount() {
            return PictureSelectorActivityEx.this.mItemMap.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long)position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            PictureSelectorActivityEx.CatalogAdapter.ViewHolder holder;
            if(convertView == null) {
                view = this.mInflater.inflate(io.rong.imkit.R.layout.rc_picsel_catalog_listview, parent, false);
                holder = new PictureSelectorActivityEx.CatalogAdapter.ViewHolder(null);
                holder.image = (ImageView)view.findViewById(io.rong.imkit.R.id.image);
                holder.name = (TextView)view.findViewById(io.rong.imkit.R.id.name);
                holder.number = (TextView)view.findViewById(io.rong.imkit.R.id.number);
                holder.selected = (ImageView)view.findViewById(io.rong.imkit.R.id.selected);
                view.setTag(holder);
            } else {
                holder = (PictureSelectorActivityEx.CatalogAdapter.ViewHolder)convertView.getTag();
            }

            String path;
            if(holder.image.getTag() != null) {
                path = (String)holder.image.getTag();
                AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
            }

            int num = 0;
            boolean showSelected = false;
            String name;
            Bitmap bitmap;
            BitmapDrawable bd;
            if(position == 0) {
                if(PictureSelectorActivityEx.this.mItemMap.size() == 0) {
                    holder.image.setImageResource(io.rong.imkit.R.drawable.rc_picsel_empty_pic);
                } else {
                    path = itemUri((PictureSelectorActivity.PicItem)((List) PictureSelectorActivityEx.this.mItemMap.get(PictureSelectorActivityEx.this.mCatalogList.get(0))).get(0));
                    AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
                    holder.image.setTag(path);
                    bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, PictureSelectorActivityEx.this.perWidth, PictureSelectorActivityEx.this.perWidth, new AlbumBitmapCacheHelper.ILoadImageCallback() {
                        public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
                            if(bitmap != null) {
                                BitmapDrawable bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                                View v = PictureSelectorActivityEx.this.mGridView.findViewWithTag(path1);
                                if(v != null) {
                                    v.setBackgroundDrawable(bd);
                                    PictureSelectorActivityEx.CatalogAdapter.this.notifyDataSetChanged();
                                }

                            }
                        }
                    }, new Object[]{Integer.valueOf(position)});
                    if(bitmap != null) {
                        bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                        holder.image.setBackgroundDrawable(bd);
                    } else {
                        holder.image.setBackgroundResource(io.rong.imkit.R.drawable.rc_grid_image_default);
                    }
                }

                name = PictureSelectorActivityEx.this.getResources().getString(io.rong.imkit.R.string.rc_picsel_catalog_allpic);
                holder.number.setVisibility(View.GONE);
                showSelected = PictureSelectorActivityEx.this.mCurrentCatalog.isEmpty();
            } else {
                path = itemUri((PictureSelectorActivity.PicItem)((List) PictureSelectorActivityEx.this.mItemMap.get(PictureSelectorActivityEx.this.mCatalogList.get(position))).get(0));
                name = (String) PictureSelectorActivityEx.this.mCatalogList.get(position);
                num = ((List) PictureSelectorActivityEx.this.mItemMap.get(PictureSelectorActivityEx.this.mCatalogList.get(position))).size();
                holder.number.setVisibility(View.VISIBLE);
                showSelected = name.equals(PictureSelectorActivityEx.this.mCurrentCatalog);
                AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
                holder.image.setTag(path);
                bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, PictureSelectorActivityEx.this.perWidth, PictureSelectorActivityEx.this.perWidth, new AlbumBitmapCacheHelper.ILoadImageCallback() {
                    public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
                        if(bitmap != null) {
                            BitmapDrawable bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                            View v = PictureSelectorActivityEx.this.mGridView.findViewWithTag(path1);
                            if(v != null) {
                                v.setBackgroundDrawable(bd);
                                PictureSelectorActivityEx.CatalogAdapter.this.notifyDataSetChanged();
                            }

                        }
                    }
                }, new Object[]{Integer.valueOf(position)});
                if(bitmap != null) {
                    bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                    holder.image.setBackgroundDrawable(bd);
                } else {
                    holder.image.setBackgroundResource(io.rong.imkit.R.drawable.rc_grid_image_default);
                }
            }

            holder.name.setText(name);
            holder.number.setText(String.format(PictureSelectorActivityEx.this.getResources().getString(io.rong.imkit.R.string.rc_picsel_catalog_number), new Object[]{Integer.valueOf(num)}));
            holder.selected.setVisibility(showSelected ?View.VISIBLE : View.GONE);
            return view;
        }

        private class ViewHolder {
            ImageView image;
            TextView name;
            TextView number;
            ImageView selected;

            private ViewHolder(Object o) {
            }
        }
    }

    private class GridViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater = PictureSelectorActivityEx.this.getLayoutInflater();

        public GridViewAdapter() {
        }

        public int getCount() {
            int sum = 0;
            String key;
            if(PictureSelectorActivityEx.this.mCurrentCatalog.isEmpty()) {
                for(Iterator var2 = PictureSelectorActivityEx.this.mItemMap.keySet().iterator(); var2.hasNext(); sum += ((List) PictureSelectorActivityEx.this.mItemMap.get(key)).size()) {
                    key = (String)var2.next();
                }
            } else {
                sum += ((List) PictureSelectorActivityEx.this.mItemMap.get(PictureSelectorActivityEx.this.mCurrentCatalog)).size();
            }

            return sum;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long)position;
        }

        @TargetApi(23)
        public View getView(int position, View convertView, ViewGroup parent) {
//            if(position == 0) {
//                View item1 = this.mInflater.inflate(io.rong.imkit.R.layout.rc_picsel_grid_camera, parent, false);
//                ImageButton view1 = (ImageButton)item1.findViewById(io.rong.imkit.R.id.camera_mask);
//                view1.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        String[] permissions = new String[]{"android.permission.CAMERA"};
//                        if(PermissionCheckUtil.requestPermissions(PictureSelectorActivityEx.this, permissions, 100)) {
//                            PictureSelectorActivityEx.this.requestCamera();
//                        }
//                    }
//                });
//                return item1;
//            } else {
                final PictureSelectorActivity.PicItem item;
                if(PictureSelectorActivityEx.this.mCurrentCatalog.isEmpty()) {
                    item = (PictureSelectorActivity.PicItem) PictureSelectorActivityEx.this.mAllItemList.get(position);
                } else {
                    item = PictureSelectorActivityEx.this.getItemAt(PictureSelectorActivityEx.this.mCurrentCatalog, position);
                }

                View view = convertView;
                final PictureSelectorActivityEx.GridViewAdapter.ViewHolder holder;
                if(convertView != null && convertView.getTag() != null) {
                    holder = (PictureSelectorActivityEx.GridViewAdapter.ViewHolder)convertView.getTag();
                } else {
                    view = this.mInflater.inflate(io.rong.imkit.R.layout.rc_picsel_grid_item, parent, false);
                    holder = new PictureSelectorActivityEx.GridViewAdapter.ViewHolder(null);
                    holder.image = (ImageView)view.findViewById(io.rong.imkit.R.id.image);
                    holder.mask = view.findViewById(io.rong.imkit.R.id.mask);
                    holder.checkBox = (PictureSelectorActivity.SelectBox)view.findViewById(io.rong.imkit.R.id.checkbox);
                    view.setTag(holder);
                }

                String path;
                if(holder.image.getTag() != null) {
                    path = (String)holder.image.getTag();
                    AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
                }

                path = itemUri(item);
                AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
                holder.image.setTag(path);
                Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, PictureSelectorActivityEx.this.perWidth, PictureSelectorActivityEx.this.perWidth, new AlbumBitmapCacheHelper.ILoadImageCallback() {
                    public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
                        if(bitmap != null) {
                            BitmapDrawable bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                            View v = PictureSelectorActivityEx.this.mGridView.findViewWithTag(path1);
                            if(v != null) {
                                v.setBackgroundDrawable(bd);
                            }

                        }
                    }
                }, new Object[]{Integer.valueOf(position)});
                if(bitmap != null) {
                    BitmapDrawable bd = new BitmapDrawable(PictureSelectorActivityEx.this.getResources(), bitmap);
                    holder.image.setBackgroundDrawable(bd);
                } else {
                    holder.image.setBackgroundResource(io.rong.imkit.R.drawable.rc_grid_image_default);
                }

                holder.checkBox.setChecked(isItemSelected(item));
                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(!holder.checkBox.getChecked() && PictureSelectorActivityEx.this.getTotalSelectedNum() == 9) {
                            Toast.makeText(PictureSelectorActivityEx.this.getApplicationContext(), io.rong.imkit.R.string.rc_picsel_selected_max, Toast.LENGTH_SHORT).show();
                        } else {
                            holder.checkBox.setChecked(!holder.checkBox.getChecked());
                            itemSelectedValue(item, holder.checkBox.getChecked());
                            if(isItemSelected(item)) {
                                holder.mask.setBackgroundColor(PictureSelectorActivityEx.this.getResources().getColor(io.rong.imkit.R.color.rc_picsel_grid_mask_pressed));
                            } else {
                                holder.mask.setBackgroundDrawable(PictureSelectorActivityEx.this.getResources().getDrawable(io.rong.imkit.R.drawable.rc_sp_grid_mask));
                            }

                            PictureSelectorActivityEx.this.updateToolbar();
                        }
                    }
                });
                if(isItemSelected(item)) {
                    holder.mask.setBackgroundColor(PictureSelectorActivityEx.this.getResources().getColor(io.rong.imkit.R.color.rc_picsel_grid_mask_pressed));
                } else {
                    holder.mask.setBackgroundDrawable(PictureSelectorActivityEx.this.getResources().getDrawable(io.rong.imkit.R.drawable.rc_sp_grid_mask));
                }

                return view;
//            }
        }

        private class ViewHolder {
            ImageView image;
            View mask;
            PictureSelectorActivity.SelectBox checkBox;

            private ViewHolder(Object o) {
            }
        }
    }
    public boolean isItemSelected(PictureSelectorActivity.PicItem item){
        return ((Boolean) ReflectUtils.getValue(PictureSelectorActivity.PicItem.class, "selected", item));
    }

    public String itemUri(PictureSelectorActivity.PicItem item){
        return ((String) ReflectUtils.getValue(PictureSelectorActivity.PicItem.class, "uri", item));
    }

    public void itemSelectedValue(PictureSelectorActivity.PicItem item, boolean value){
       ReflectUtils.setValue(PictureSelectorActivity.PicItem.class, "selected", value ,item);
    }
    
    public void itemUriValue(PictureSelectorActivity.PicItem item, String value){
        ReflectUtils.setValue(PictureSelectorActivity.PicItem.class, "uri", value ,item);
    }
}
